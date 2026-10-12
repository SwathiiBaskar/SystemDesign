package com.example.consistent_hash.Hashing;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class ConsistentHashRing {

    private final TreeMap<Long, VirtualNode> ring = new TreeMap<>();
    private final int virtualNodeCount;

    public ConsistentHashRing(int virtualNodeCount) {
        this.virtualNodeCount = virtualNodeCount;
    }

    public void addNode(String node) {
        for (int i = 0; i < virtualNodeCount; i++) {
            VirtualNode vnode = new VirtualNode(node, i);
            ring.put(vnode.getHash(), vnode);
        }
    }

    public void removeNode(String node){
        ring.entrySet().removeIf(
            e -> e.getValue().getNode().equals(node));
    }

    public String getNode(String key){

        long hash = HashFunction.generateHash(key);

        SortedMap<Long,VirtualNode> tail = ring.tailMap(hash);

        Long target = tail.isEmpty() ?
                ring.firstKey() :
                tail.firstKey();

        return ring.get(target).getNode();
    }

    public String getNodeForKey(String id) {
        return getNode(id);
    }

    public List<VirtualNode> getRingSnapshot() {
        return new ArrayList<>(ring.values());
    }
}
