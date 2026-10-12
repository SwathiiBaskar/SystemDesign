package com.example.consistent_hash.Hashing;

import org.springframework.data.mongodb.core.MongoTemplate;

public class PhysicalNode {
    private final String name;
    private final String uri;
    private final MongoTemplate mongoTemplate;
    public PhysicalNode(String name, String uri, MongoTemplate mongoTemplate) {
        this.name = name;
        this.uri = uri;
        this.mongoTemplate = mongoTemplate;
    }
    public String getName() { return name; }
    public String getUri() { return uri; }
    public MongoTemplate getMongoTemplate() { return mongoTemplate; }
}