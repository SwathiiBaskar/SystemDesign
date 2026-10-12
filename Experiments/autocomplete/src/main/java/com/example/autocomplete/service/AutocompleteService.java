package com.example.autocomplete.service;

import com.example.autocomplete.model.AutocompleteResponse;
import com.example.autocomplete.trie.Trie;
import com.example.autocomplete.trie.TrieNode;

import jakarta.annotation.PostConstruct;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class AutocompleteService {

    private final Trie trie;
    private final StringRedisTemplate redisTemplate;
    private static final String CACHE_PREFIX = "autocomplete:";

    // CONSTRUCTOR
    public AutocompleteService(StringRedisTemplate redisTemplate) {
        this.trie = new Trie();
        this.redisTemplate = redisTemplate;
    }

    // LOAD DATASET
    @PostConstruct
    public void loadDataset() {
        try {
            InputStream inputStream = getClass()
                            .getClassLoader()
                            .getResourceAsStream( "data/search_terms.csv");

            if (inputStream == null) {
                throw new RuntimeException(  "search_terms.csv not found");
            }

            BufferedReader reader = new BufferedReader(
                            new InputStreamReader( inputStream,StandardCharsets.UTF_8));

            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts =  line.split(",");
                if (parts.length < 2) {
                    continue;
                }
                String term = parts[0].trim();
                int frequency =  Integer.parseInt( parts[1].trim());
                trie.insert(  term, frequency);
            }
            reader.close();

            System.out.println( "Dataset successfully loaded into Trie.");

        } catch (Exception e) {
            throw new RuntimeException( "Error loading dataset",  e);
        }
    }

    // AUTOCOMPLETE
    public AutocompleteResponse autocomplete(String prefix,int k) {

        prefix = prefix.toLowerCase().trim();

        if (k <= 0) {
            k = 5;
        }

        // Create Redis key
        String cacheKey =CACHE_PREFIX+ prefix+ ":"+ k;

        // STEP 1: Check Redis
        String cached = redisTemplate.opsForValue() .get(cacheKey);

        // STEP 2: Redis HIT
        if (cached != null) {
            @SuppressWarnings("unchecked")
            List<String> suggestions = deserialize(cached);
            return new AutocompleteResponse( prefix,suggestions,"redis");
        }

        // STEP 3: Redis MISS
        // Search Trie
        List<TrieNode> matchingNodes = trie.searchPrefix(prefix);

        // STEP 4: Sort by frequency
        matchingNodes.sort(Comparator.comparingInt(  TrieNode::getFrequency) .reversed());

        // STEP 5: Select Top-K
        List<String> suggestions = new ArrayList<>();
        int limit = Math.min( k, matchingNodes.size());

        for (int i = 0;i < limit;i++) {
            suggestions.add(matchingNodes.get(i).getWord());
        }
        String serialized = serialize(suggestions);

        // STEP 6: Store in Redis
        redisTemplate.opsForValue().set( cacheKey,serialized);

        // STEP 7: Return response
        return new AutocompleteResponse(prefix,suggestions,"trie");
    }

    // Serialize List<String> into a single String
    private String serialize(List<String> suggestions) {
        return String.join("|||", suggestions);
    }


    // Deserialize String back into List<String>
    private List<String> deserialize(String cached) {
        if (cached == null || cached.isEmpty()) {
            return new ArrayList<>();
        }
        return new ArrayList<>(
                List.of(cached.split("\\|\\|\\|"))
        );
    }
}