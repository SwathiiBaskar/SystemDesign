package com.lab.crawler;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.net.InetAddress;
import java.net.URI;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * Redis layout (per job id):
 *  job:{id}          HASH  status + counters
 *  job:{id}:queue    LIST  URL frontier  ("url|depth")
 *  job:{id}:seen     SET   every URL ever queued (dedupe at discovery)
 *  job:{id}:visited  SET   URLs actually fetched (dedupe at crawl)
 *  job:{id}:pages    LIST  crawl order of visited URLs
 *  page:{url}        STRING JSON page cache, shared across jobs (TTL 1h)
 */
@Service
public class CrawlerService {
  private static final Set<String> SKIP = Set.of("jpg","jpeg","png","gif","pdf","zip","css","js","svg","mp4","mp3","ico","webp","exe");
  private final StringRedisTemplate r;
  private final ObjectMapper om = new ObjectMapper();
  private final ExecutorService pool = Executors.newFixedThreadPool(8);
  private final Map<String, Boolean> hostOk = new ConcurrentHashMap<>();

  public CrawlerService(StringRedisTemplate r) { this.r = r; }

  public String start(String seed, int max, int depth, boolean same, int delay) {
    seed = normalize(seed);
    if (seed == null || !allowed(seed)) throw new IllegalArgumentException("URL not permitted: " + seed);
    String id = UUID.randomUUID().toString().substring(0, 8), k = "job:" + id;
    Map<String, String> m = new HashMap<>();
    m.put("id", id); m.put("status", "RUNNING"); m.put("seed", seed);
    m.put("max", "" + Math.min(max, 200)); m.put("depth", "" + Math.min(depth, 4)); m.put("same", "" + same);
    m.put("delay", "" + Math.max(delay, 0)); m.put("visited", "0"); m.put("discovered", "1");
    m.put("cacheHits", "0"); m.put("failed", "0"); m.put("started", "" + System.currentTimeMillis());
    r.opsForHash().putAll(k, m);
    r.opsForSet().add(k + ":seen", seed);
    r.opsForList().rightPush(k + ":queue", seed + "|0");
    pool.submit(() -> run(id));
    return id;
  }

  @SuppressWarnings("unchecked")
  private void run(String id) {
    String k = "job:" + id;
    var h = r.opsForHash();
    try {
      String seed = (String) h.get(k, "seed");
      int max = Integer.parseInt((String) h.get(k, "max")), maxDepth = Integer.parseInt((String) h.get(k, "depth"));
      long delay = Long.parseLong((String) h.get(k, "delay"));
      boolean same = Boolean.parseBoolean((String) h.get(k, "same"));
      String host = URI.create(seed).getHost();
      int done = 0; String item;
      while (done < max && (item = r.opsForList().leftPop(k + ":queue")) != null) {
        String[] p = item.split("\\|"); String url = p[0]; int d = Integer.parseInt(p[1]);
        Long fresh = r.opsForSet().add(k + ":visited", url);
        if (fresh == null || fresh == 0) continue;               // already crawled -> skip
        Map<String, Object> page = load(k, url);
        r.opsForList().rightPush(k + ":pages", url);
        h.increment(k, "visited", 1); done++;
        if (d < maxDepth) for (String l : (List<String>) page.get("links")) {
          if (!allowed(l) || (same && !Objects.equals(host, URI.create(l).getHost()))) continue;
          Long isNew = r.opsForSet().add(k + ":seen", l);
          if (isNew != null && isNew == 1) { r.opsForList().rightPush(k + ":queue", l + "|" + (d + 1)); h.increment(k, "discovered", 1); }
        }
        if (delay > 0) Thread.sleep(delay);
      }
    } catch (Exception ignored) {
    } finally {
      h.put(k, "status", "DONE"); h.put(k, "finished", "" + System.currentTimeMillis());
    }
  }

  private Map<String, Object> load(String k, String url) throws Exception {
    String cached = r.opsForValue().get("page:" + url);
    if (cached != null) { r.opsForHash().increment(k, "cacheHits", 1); return om.readValue(cached, Map.class); }
    Map<String, Object> pg = new LinkedHashMap<>();
    pg.put("url", url);
    long t = System.currentTimeMillis();
    try {
      Connection.Response res = Jsoup.connect(url).userAgent("LabCrawler/1.0 (+education)").timeout(8000)
          .maxBodySize(2_000_000).ignoreContentType(true).followRedirects(true).execute();
      String ct = res.contentType();
      if (ct == null || !ct.contains("html")) throw new IllegalStateException("Not HTML: " + ct);
      Document doc = res.parse();
      Safelist sl = Safelist.relaxed().addTags("section", "article", "header", "footer", "nav", "main", "figure", "figcaption");
      String html = Jsoup.clean(doc.body().html(), url, sl);
      Element meta = doc.selectFirst("meta[name=description]");
      pg.put("status", "OK"); pg.put("httpStatus", res.statusCode());
      pg.put("title", doc.title().isBlank() ? url : doc.title());
      pg.put("description", meta == null ? "" : meta.attr("content"));
      String text = doc.body().text();
      pg.put("text", text.length() > 4000 ? text.substring(0, 4000) : text);
      pg.put("html", html.length() > 200_000 ? html.substring(0, 200_000) : html);
      pg.put("links", doc.select("a[href]").stream().map(a -> normalize(a.absUrl("href")))
          .filter(Objects::nonNull).distinct().collect(Collectors.toList()));
      pg.put("images", doc.select("img[src]").stream().map(i -> i.absUrl("src")).filter(s -> s.startsWith("http")).limit(6).toList());
      pg.put("size", res.bodyAsBytes().length);
    } catch (Exception e) {
      r.opsForHash().increment(k, "failed", 1);
      pg.put("status", "FAILED"); pg.put("error", String.valueOf(e.getMessage())); pg.put("title", url);
      pg.put("description", ""); pg.put("text", ""); pg.put("html", ""); pg.put("links", List.of()); pg.put("images", List.of()); pg.put("size", 0);
    }
    pg.put("timeMs", System.currentTimeMillis() - t);
    if ("OK".equals(pg.get("status"))) r.opsForValue().set("page:" + url, om.writeValueAsString(pg), Duration.ofHours(1));
    return pg;
  }

  /** Validation: http(s) only, no binary extensions, no private/loopback hosts (SSRF guard). */
  public boolean allowed(String url) {
    try {
      URI u = URI.create(url);
      if (u.getHost() == null || !(u.getScheme().equals("http") || u.getScheme().equals("https"))) return false;
      String path = u.getPath() == null ? "" : u.getPath().toLowerCase();
      int dot = path.lastIndexOf('.');
      if (dot >= 0 && SKIP.contains(path.substring(dot + 1))) return false;
      return hostOk.computeIfAbsent(u.getHost(), hn -> {
        try { InetAddress a = InetAddress.getByName(hn);
          return !(a.isLoopbackAddress() || a.isSiteLocalAddress() || a.isLinkLocalAddress() || a.isAnyLocalAddress());
        } catch (Exception e) { return false; } });
    } catch (Exception e) { return false; }
  }

  private String normalize(String s) {
    try { if (s == null || s.isBlank()) return null;
      URI u = new URI(s.trim()); return new URI(u.getScheme(), u.getAuthority(), u.getPath(), u.getQuery(), null).toString();
    } catch (Exception e) { return null; }
  }

  public Map<Object, Object> job(String id) { Map<Object, Object> m = r.opsForHash().entries("job:" + id); return m.isEmpty() ? null : m; }

  public Map<String, Object> page(String url) throws Exception {
    String j = r.opsForValue().get("page:" + url);
    return j == null ? null : om.readValue(j, Map.class);
  }

  /** Page summaries for the UI (html omitted), starting at crawl-order index `from`. */
  public List<Map<String, Object>> pages(String id, int from) throws Exception {
    List<String> urls = r.opsForList().range("job:" + id + ":pages", from, -1);
    List<Map<String, Object>> out = new ArrayList<>();
    if (urls == null) return out;
    for (String u : urls) {
      Map<String, Object> p = page(u);
      if (p == null) { p = new LinkedHashMap<>(); p.put("url", u); p.put("title", u); p.put("status", "FAILED"); p.put("text", ""); p.put("links", List.of()); p.put("images", List.of()); }
      p = new LinkedHashMap<>(p); p.remove("html"); p.put("linkCount", ((List<?>) p.get("links")).size()); p.remove("links");
      out.add(p);
    }
    return out;
  }

  public Map<String, Object> stats() {
    Set<String> jobs = r.keys("job:????????"); Map<String, Object> m = new HashMap<>();
    m.put("jobs", jobs == null ? 0 : jobs.size()); Set<String> pc = r.keys("page:*"); m.put("cachedPages", pc == null ? 0 : pc.size());
    return m;
  }
}
