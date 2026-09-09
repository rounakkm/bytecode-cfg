package com.bytecodecfg.cfg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class CfgResult {

    public final String methodId;

    public final List<BasicBlock> blocks;

    public final Map<Integer, Map<Integer, String>> edgeLabels;

    public CfgResult(String methodId, List<BasicBlock> blocks,
                     Map<Integer, Map<Integer, String>> edgeLabels) {
        this.methodId = methodId;
        this.blocks = java.util.Collections.unmodifiableList(blocks);
        this.edgeLabels = java.util.Collections.unmodifiableMap(edgeLabels);
    }


    public String getEdgeLabel(int fromId, int toId) {
        Map<Integer, String> inner = edgeLabels.get(fromId);
        if (inner == null) return "";
        return inner.getOrDefault(toId, "");
    }

    
    static Map<Integer, Map<Integer, String>> newEdgeLabelMap() {
        return new HashMap<>();
    }


    static void putEdgeLabel(Map<Integer, Map<Integer, String>> labels,
                             BasicBlock from, BasicBlock to, String label) {
        labels.computeIfAbsent(from.getId(), k -> new HashMap<>())
              .put(to.getId(), label);
    }
}
