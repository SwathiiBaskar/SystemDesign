package com.example.consistent_hash.Hashing;

public class VirtualNode {

    private String node;
    private long hash;

    public VirtualNode(String node, int replica) {
        this.node = node;
        this.hash = HashFunction.generateHash(node + "#" + replica);
    }

    public String getNode() {
        return node;
    }

    public long getHash() {
        return hash;
    }
}
