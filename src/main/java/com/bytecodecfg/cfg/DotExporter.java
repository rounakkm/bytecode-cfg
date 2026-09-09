package com.bytecodecfg.cfg;

import java.util.List;
import java.util.Map;

public class DotExporter {

    public String export(CfgResult result) {
        StringBuilder sb = new StringBuilder();

       
        String graphId = result.methodId.replaceAll("[^A-Za-z0-9_]", "_");
        sb.append("digraph ").append(graphId).append(" {\n");
        sb.append("    graph [rankdir=TB fontname=\"Helvetica\"];\n");
        sb.append("    node  [fontname=\"Helvetica\" fontsize=10];\n");
        sb.append("    edge  [fontname=\"Helvetica\" fontsize=9];\n");
        sb.append("\n");

        List<BasicBlock> blocks = result.blocks;

        for (BasicBlock block : blocks) {
            String nodeId    = "B" + block.getId();
            String shape     = nodeShape(block);
            String label     = nodeLabel(block);
            sb.append("    ").append(nodeId)
              .append(" [shape=").append(shape)
              .append(" label=\"").append(escapeDot(label)).append("\"")
              .append(fillColor(block))
              .append("];\n");
        }

        sb.append("\n");

   
        for (BasicBlock block : blocks) {
            List<BasicBlock> successors = block.getSuccessors();
            for (BasicBlock succ : successors) {
                String fromId = "B" + block.getId();
                String toId   = "B" + succ.getId();
                String edgeLabel = result.getEdgeLabel(block.getId(), succ.getId());

                sb.append("    ").append(fromId).append(" -> ").append(toId);
                if (!edgeLabel.isEmpty()) {
                    sb.append(" [label=\"").append(escapeDot(edgeLabel)).append("\"]");
                }
                sb.append(";\n");
            }
        }

        sb.append("}\n");
        return sb.toString();
    }

    private String nodeShape(BasicBlock block) {
        switch (block.getType()) {
            case ENTRY: return "doublecircle";
            case EXIT:  return "doublecircle";
            default:    return "box";
        }
    }

    private String nodeLabel(BasicBlock block) {
        switch (block.getType()) {
            case ENTRY: return "ENTRY";
            case EXIT:  return "EXIT";
            default:    break;
        }

        List<String> stmts = block.getStatements();
        if (stmts.isEmpty()) {
            return "(empty)";
        }

        StringBuilder label = new StringBuilder();
        for (int i = 0; i < stmts.size(); i++) {
            if (i > 0) label.append("\\n");
            label.append(stmts.get(i));
        }
        return label.toString();
    }

    private String fillColor(BasicBlock block) {
        switch (block.getType()) {
            case ENTRY: return " style=filled fillcolor=\"#aee8c0\"";
            case EXIT:  return " style=filled fillcolor=\"#f4a8a8\""; 
            default:    return "";
        }
    }


    private String escapeDot(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "");
    }
}
