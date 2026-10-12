package com.example.autocomplete.controller;

import com.example.autocomplete.model.AutocompleteResponse;
import com.example.autocomplete.service.AutocompleteService;

import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin("*")
@RestController
public class AutocompleteController {

    private final AutocompleteService service;
    public AutocompleteController( AutocompleteService service) {
        this.service = service;
    }

    @GetMapping("/api/autocomplete")
    public AutocompleteResponse autocomplete(@RequestParam String prefix,@RequestParam(defaultValue = "5")int k) {
        return service.autocomplete(prefix,k);
    }
}