package com.lab.crawler;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/api")
public class CrawlController {
  private final CrawlerService svc;
  public CrawlController(CrawlerService svc) { this.svc = svc; }

  public record CrawlRequest(String seed, Integer maxPages, Integer maxDepth, Boolean sameDomain, Integer delayMs) {}

  @PostMapping("/crawl")
  public ResponseEntity<?> start(@RequestBody CrawlRequest q) {
    try {
      String id = svc.start(q.seed(), q.maxPages() == null ? 10 : q.maxPages(), q.maxDepth() == null ? 1 : q.maxDepth(),
          q.sameDomain() == null || q.sameDomain(), q.delayMs() == null ? 200 : q.delayMs());
      return ResponseEntity.accepted().body(Map.of("jobId", id));
    } catch (IllegalArgumentException e) { return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); }
  }

  @GetMapping("/crawl/{id}")
  public ResponseEntity<?> job(@PathVariable String id) {
    var j = svc.job(id); return j == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(j);
  }

  @GetMapping("/crawl/{id}/pages")
  public List<Map<String, Object>> pages(@PathVariable String id, @RequestParam(defaultValue = "0") int from) throws Exception {
    return svc.pages(id, from);
  }

  /** Full stored page including sanitized HTML, for the reader pane. */
  @GetMapping("/page")
  public ResponseEntity<?> page(@RequestParam String url) throws Exception {
    var p = svc.page(url); return p == null ? ResponseEntity.notFound().build() : ResponseEntity.ok(p);
  }

  @GetMapping("/stats") public Map<String, Object> stats() { return svc.stats(); }
}
