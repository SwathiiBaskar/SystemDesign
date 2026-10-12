package com.example.urlshortener.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.stereotype.Service;

import com.example.urlshortener.Model.URLModel;
import com.example.urlshortener.Repository.Repo;

import java.util.Random;

@Service
public class URLService {
    private final Repo repo;

    public URLService(Repo repo){
        this.repo=repo;
    }

    private static int requestCount = 0;
    private static long totalTime = 0;
    private static int dbAccessCount = 0;

    public String shortURL(String longURL){
        URLModel existing = repo.findByLongURL(longURL);
        String input = longURL;

        if(existing!=null){
            return existing.getShortURL();
        }

        String shortCode = generateHash(longURL);

        while(repo.findByShortURL(shortCode)!=null){
            input = input+new Random().nextInt(1000);
            shortCode = generateHash(input);
        }

        URLModel url = new URLModel();
        url.setLongURL(longURL);
        url.setShortURL(shortCode);
        repo.save(url);

        return shortCode;
    }

    public String getLongURL(String shortCode){

        requestCount++;

    System.out.println("\n==============================================");
    System.out.println("Request Number : " + requestCount);
    System.out.println("Short Code     : " + shortCode);

    long start = System.nanoTime();

        URLModel url = repo.findByShortURL(shortCode);
        
        dbAccessCount++;

    long end = System.nanoTime();

    long duration = end - start;

    totalTime += duration;

    System.out.println("Source          : MongoDB");
    System.out.println("Database Access : " + dbAccessCount);
    System.out.printf("Response Time   : %.3f ms%n", duration / 1_000_000.0);
    System.out.printf("Average Time    : %.3f ms%n",
            (totalTime / (double) requestCount) / 1_000_000.0);

    System.out.println("==============================================");

        if(url!=null){
            return url.getLongURL();
        }
        return null;
    }

    private String generateHash(String input){
        try{
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();

            for(byte b: hash){
                sb.append(String.format("%02x",b));
            }

            return sb.substring(0,6);
        }catch(NoSuchAlgorithmException e){
            throw new RuntimeException(e);
        }
    }

}
