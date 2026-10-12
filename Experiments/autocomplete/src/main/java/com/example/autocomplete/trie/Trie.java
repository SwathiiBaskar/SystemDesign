package com.example.autocomplete.trie;

import java.util.ArrayList;
import java.util.List;

public class Trie {

    private final TrieNode root;

    public Trie() {
        root = new TrieNode();
    }


    // =========================================
    // INSERT
    // =========================================

    public void insert(String word, int frequency) {

        TrieNode current = root;
        for (char ch : word.toLowerCase().toCharArray()) {
            if (!current.getChildren().containsKey(ch)) {
                current.getChildren().put(ch, new TrieNode());
            }
            current = current.getChildren().get(ch);
        }

        current.setEndOfWord(true);
        current.setWord(word);
        current.setFrequency(frequency);
    }


    // =========================================
    // FIND PREFIX NODE
    // =========================================

    private TrieNode findPrefixNode(String prefix) {

        TrieNode current = root;
        for (char ch : prefix.toLowerCase().toCharArray()) {
            if (!current.getChildren().containsKey(ch)) {
                return null;
            }
            current = current.getChildren().get(ch);
        }
        return current;
    }


    // =========================================
    // COLLECT WORDS
    // =========================================

    private void collectWords(TrieNode node, List<TrieNode> results) {
        if (node.isEndOfWord()) {
            results.add(node);
        }
        for (TrieNode child : node.getChildren().values()) {
            collectWords(child, results);
        }
    }


    // =========================================
    // PREFIX SEARCH
    // =========================================

    public List<TrieNode> searchPrefix(String prefix) {

        List<TrieNode> results = new ArrayList<>();
        TrieNode prefixNode = findPrefixNode(prefix);
        if (prefixNode == null) {
            return results;
        }
        collectWords(prefixNode, results);
        return results;
    }
}