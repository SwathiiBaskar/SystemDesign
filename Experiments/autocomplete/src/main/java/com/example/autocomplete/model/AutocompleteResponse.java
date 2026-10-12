package com.example.autocomplete.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class AutocompleteResponse {

    private String prefix;
    private List<String> suggestions;
    private String source;
}