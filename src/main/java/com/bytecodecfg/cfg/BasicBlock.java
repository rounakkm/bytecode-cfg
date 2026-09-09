package com.bytecodecfg.cfg;

import java.util.ArrayList;
import java.util.List;

public class BasicBlock {

    
    public enum BlockType {
        
        ENTRY,
        
        NORMAL,
        
        EXIT
    }

    private static int nextId = 0;

    private final int id;

    
    private final BlockType type;


    private final List<String> statements;

    private final List<BasicBlock> successors;


    public BasicBlock(int id, BlockType type) {
        this.id = id;
        this.type = type;
        this.statements = new ArrayList<>();
        this.successors = new ArrayList<>();
    }


    static BasicBlock newNormal() {
        return new BasicBlock(nextId++, BlockType.NORMAL);
    }

    
    static BasicBlock newEntry() {
        return new BasicBlock(nextId++, BlockType.ENTRY);
    }

    
    static BasicBlock newExit() {
        return new BasicBlock(nextId++, BlockType.EXIT);
    }


    static void resetIdCounter() {
        nextId = 0;
    }

    void addStatement(String summary) {
        statements.add(summary);
    }


    void addSuccessor(BasicBlock target) {
        successors.add(target);
    }

    public int getId() {
        return id;
    }


    public BlockType getType() {
        return type;
    }
    public List<String> getStatements() {
        return java.util.Collections.unmodifiableList(statements);
    }


    public List<BasicBlock> getSuccessors() {
        return java.util.Collections.unmodifiableList(successors);
    }

    public boolean isEmpty() {
        return statements.isEmpty();
    }

    @Override
    public String toString() {
        return String.format("Block[%d, %s, stmts=%d, succs=%s]",
                id, type, statements.size(),
                successors.stream().map(b -> String.valueOf(b.id))
                        .collect(java.util.stream.Collectors.joining(",")));
    }
}
