package com.example.urlshortener.Controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.urlshortener.Service.URLService;
import com.example.urlshortener.dto.UrlRequest;
import com.example.urlshortener.dto.UrlResponse;

import java.net.URI;
import org.springframework.http.HttpHeaders;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@CrossOrigin(origins = "http://localhost:5173")
@RestController
public class URLController {
    private final URLService urlService;

    public URLController(URLService urlService){
        this.urlService = urlService;
    }

    @PostMapping("/shorten")
    public UrlResponse shorten(@RequestBody UrlRequest request) {

        String code = urlService.shortURL(request.getLongUrl());
        return new UrlResponse("http://localhost:8080/" + code);

    }

    @GetMapping("/{shortCode}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {

        String url = urlService.getLongURL(shortCode);

        if (url == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();

        headers.setLocation(URI.create(url));

        return ResponseEntity.status(302).headers(headers).build();

    }

    
}
