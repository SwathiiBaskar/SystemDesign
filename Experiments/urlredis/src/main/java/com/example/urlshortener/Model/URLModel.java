package com.example.urlshortener.Model;

import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.Data;

@Data
@Document(collection="urlmapping")

public class URLModel{
    String longURL;

    @Indexed(unique=true)
    String shortURL;
}