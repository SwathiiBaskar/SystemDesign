package com.example.urlshortener.Repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.urlshortener.Model.URLModel;




@Repository
public interface Repo extends MongoRepository<URLModel,String> {
    public URLModel findByShortURL(String shortURL);
    public URLModel findByLongURL(String longURL);
}
