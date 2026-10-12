package com.example.rate_limiter.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.rate_limiter.service.RateLimiterService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final RateLimiterService rateLimiterService;

    public ApiController(RateLimiterService rateLimiterService) {
        this.rateLimiterService = rateLimiterService;
    }

    @GetMapping("/hello")
    public ResponseEntity<Map<String, Object>> hello(
            @RequestHeader(value = "X-Client-ID", required = false)
            String clientId,
            HttpServletRequest request) {

        if (clientId == null || clientId.isBlank()) {
            clientId = request.getRemoteAddr();
        }

        boolean allowed = rateLimiterService.isAllowed(clientId);

        Map<String, Object> response = new HashMap<>();

        if (allowed) {

            response.put("status", "success");
            response.put("message", "Request allowed");
            response.put("clientId", clientId);

            return ResponseEntity.ok(response);
        }

        response.put("status", "error");
        response.put("message", "Rate limit exceeded");
        response.put("clientId", clientId);

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .header("Retry-After", "1")
                .body(response);
    }

    @GetMapping("/config")
    public Map<String, Object> configuration() {

        Map<String, Object> response = new HashMap<>();

        response.put(
                "bucketCapacity",
                rateLimiterService.getBucketCapacity()
        );

        response.put(
                "refillRate",
                rateLimiterService.getRefillRate()
        );

        return response;
    }
}
