package com.example.autocomplete.trie;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class TrieNode {

    private Map<Character, TrieNode> children = new HashMap<>();
    private boolean endOfWord;
    private String word;
    private int frequency;
}