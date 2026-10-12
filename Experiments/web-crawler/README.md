# Lab 8 – Spring Boot Web Crawler (Redis + Docker)

Run:  `docker compose up --build`  →  UI at http://localhost:8080

## REST API (Postman)
| Method | Path | Purpose |
|---|---|---|
| POST | /api/crawl | body `{"seed":"https://quotes.toscrape.com","maxPages":10,"maxDepth":1,"sameDomain":true,"delayMs":200}` → `{"jobId":"ab12cd34"}` (202) |
| GET | /api/crawl/{id} | status + counters (visited, discovered, cacheHits, failed) |
| GET | /api/crawl/{id}/pages?from=0 | crawled pages (title, text, images, timing) |
| GET | /api/page?url=... | one full page incl. sanitized HTML |
| GET | /api/stats | jobs and cached pages in Redis |

Invalid seed (e.g. `http://localhost`, `ftp://x`) → 400.

## JMeter
Thread Group: 50 threads, ramp-up 10 s, loop 5.
HTTP Request: POST `localhost:8080/api/crawl`, Body Data:
`{"seed":"https://quotes.toscrape.com","maxPages":5,"maxDepth":1,"delayMs":0}`, header `Content-Type: application/json`.
Add: Summary Report, Aggregate Report, Response Times Over Time.
Run three workloads (10 / 50 / 100 threads) and compare throughput, avg/95th latency, error %.
Repeat with Redis cache warm vs. `docker exec -it <redis> redis-cli FLUSHALL` to show cache impact.
