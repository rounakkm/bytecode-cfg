package com.bytecodecfg.cfg;

import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.WhileStmt;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class CfgBuilder {

    
    public CfgResult build(MethodDeclaration method, String methodId) {
        BasicBlock.resetIdCounter();

        List<BasicBlock> allBlocks = new ArrayList<>();
        Map<Integer, Map<Integer, String>> edgeLabels = CfgResult.newEdgeLabelMap();

        
        BasicBlock entry = BasicBlock.newEntry();
        BasicBlock exit  = BasicBlock.newExit();
        allBlocks.add(entry);
        allBlocks.add(exit);

        
        BasicBlock firstReal = BasicBlock.newNormal();
        allBlocks.add(firstReal);
        entry.addSuccessor(firstReal);

        
        if (method.getBody().isPresent()) {
            BlockStmt body = method.getBody().get();
            Context ctx = new Context(exit, allBlocks, edgeLabels, null, null);
            BasicBlock last = walkStatements(body.getStatements(), firstReal, ctx);
            if (!last.getSuccessors().contains(exit)) {
                last.addSuccessor(exit);
            }
        } else {
            firstReal.addSuccessor(exit);
        }

        return new CfgResult(methodId, allBlocks, edgeLabels);
    }

    
    private static class Context {
        
        final BasicBlock exit;
        
        final List<BasicBlock> allBlocks;
       
        final Map<Integer, Map<Integer, String>> edgeLabels;
        
        final BasicBlock innerLoopHeader;
 
        final BasicBlock innerLoopExit;

        Context(BasicBlock exit, List<BasicBlock> allBlocks,
                Map<Integer, Map<Integer, String>> edgeLabels,
                BasicBlock innerLoopHeader, BasicBlock innerLoopExit) {
            this.exit = exit;
            this.allBlocks = allBlocks;
            this.edgeLabels = edgeLabels;
            this.innerLoopHeader = innerLoopHeader;
            this.innerLoopExit   = innerLoopExit;
        }

       
        Context withLoop(BasicBlock loopHeader, BasicBlock loopExit) {
            return new Context(exit, allBlocks, edgeLabels, loopHeader, loopExit);
        }
    }

    private BasicBlock walkStatements(NodeList<Statement> stmts,
                                      BasicBlock current,
                                      Context ctx) {
        for (Statement stmt : stmts) {
            current = walkStatement(stmt, current, ctx);
        }
        return current;
    }


    private BasicBlock walkStatement(Statement stmt, BasicBlock current, Context ctx) {

        if (stmt instanceof IfStmt) {
            return walkIf((IfStmt) stmt, current, ctx);

        } else if (stmt instanceof WhileStmt) {
            return walkWhile((WhileStmt) stmt, current, ctx);

        } else if (stmt instanceof ForStmt) {
            return walkFor((ForStmt) stmt, current, ctx);

        } else if (stmt instanceof DoStmt) {
            return walkDoWhile((DoStmt) stmt, current, ctx);

        } else if (stmt instanceof ForEachStmt) {
            return walkForEach((ForEachStmt) stmt, current, ctx);

        } else if (stmt instanceof ReturnStmt) {
            return walkReturn((ReturnStmt) stmt, current, ctx);

        } else if (stmt instanceof BlockStmt) {
           
            return walkStatements(((BlockStmt) stmt).getStatements(), current, ctx);

        } else if (stmt instanceof BreakStmt) {
            return walkBreak(current, ctx);

        } else if (stmt instanceof ContinueStmt) {
            return walkContinue(current, ctx);

        } else if (stmt instanceof LabeledStmt) {
           
            return walkStatement(((LabeledStmt) stmt).getStatement(), current, ctx);

        } else {
           
            String summary = summarise(stmt);
            current.addStatement(summary);
            return current;
        }
    }


    private BasicBlock walkIf(IfStmt ifStmt, BasicBlock current, Context ctx) {
        
        BasicBlock condBlock = newBlock(ctx);
        current.addSuccessor(condBlock);
        condBlock.addStatement("if (" + truncate(ifStmt.getCondition().toString()) + ")");

 
        BasicBlock thenStart = newBlock(ctx);
        condBlock.addSuccessor(thenStart);
        CfgResult.putEdgeLabel(ctx.edgeLabels, condBlock, thenStart, "true");

        BasicBlock thenEnd = walkStatement(ifStmt.getThenStmt(), thenStart, ctx);

     
        BasicBlock mergeBlock = newBlock(ctx);

        
        if (ifStmt.getElseStmt().isPresent()) {
            BasicBlock elseStart = newBlock(ctx);
            condBlock.addSuccessor(elseStart);
            CfgResult.putEdgeLabel(ctx.edgeLabels, condBlock, elseStart, "false");

            BasicBlock elseEnd = walkStatement(ifStmt.getElseStmt().get(), elseStart, ctx);
            
            if (!elseEnd.getSuccessors().contains(ctx.exit)) {
                elseEnd.addSuccessor(mergeBlock);
            }
        } else {
            
            condBlock.addSuccessor(mergeBlock);
            CfgResult.putEdgeLabel(ctx.edgeLabels, condBlock, mergeBlock, "false");
        }

      
        if (!thenEnd.getSuccessors().contains(ctx.exit)) {
            thenEnd.addSuccessor(mergeBlock);
        }

        return mergeBlock;
    }

    private BasicBlock walkWhile(WhileStmt whileStmt, BasicBlock current, Context ctx) {
        BasicBlock loopHeader = newBlock(ctx);
        current.addSuccessor(loopHeader);
        loopHeader.addStatement("while (" + truncate(whileStmt.getCondition().toString()) + ")");

        BasicBlock postLoop = newBlock(ctx);

        BasicBlock bodyStart = newBlock(ctx);
        loopHeader.addSuccessor(bodyStart);
        CfgResult.putEdgeLabel(ctx.edgeLabels, loopHeader, bodyStart, "true");
        loopHeader.addSuccessor(postLoop);
        CfgResult.putEdgeLabel(ctx.edgeLabels, loopHeader, postLoop, "false");

        Context loopCtx = ctx.withLoop(loopHeader, postLoop);
        BasicBlock bodyEnd = walkStatement(whileStmt.getBody(), bodyStart, loopCtx);

     
        if (!bodyEnd.getSuccessors().contains(ctx.exit)
                && !bodyEnd.getSuccessors().contains(postLoop)) {
            bodyEnd.addSuccessor(loopHeader);
            CfgResult.putEdgeLabel(ctx.edgeLabels, bodyEnd, loopHeader, "loop-back");
        }

        return postLoop;
    }


    private BasicBlock walkFor(ForStmt forStmt, BasicBlock current, Context ctx) {
        
        forStmt.getInitialization().forEach(init ->
                current.addStatement("for-init: " + truncate(init.toString())));

        BasicBlock loopHeader = newBlock(ctx);
        current.addSuccessor(loopHeader);

        String condition = forStmt.getCompare()
                .map(c -> truncate(c.toString()))
                .orElse("<no condition>");
        String update = forStmt.getUpdate().isEmpty() ? "" :
                "; update: " + truncate(forStmt.getUpdate().toString());
        loopHeader.addStatement("for (" + condition + update + ")");

        BasicBlock postLoop = newBlock(ctx);
        BasicBlock bodyStart = newBlock(ctx);
        loopHeader.addSuccessor(bodyStart);
        CfgResult.putEdgeLabel(ctx.edgeLabels, loopHeader, bodyStart, "true");
        loopHeader.addSuccessor(postLoop);
        CfgResult.putEdgeLabel(ctx.edgeLabels, loopHeader, postLoop, "false");

        Context loopCtx = ctx.withLoop(loopHeader, postLoop);
        BasicBlock bodyEnd = walkStatement(forStmt.getBody(), bodyStart, loopCtx);

        if (!bodyEnd.getSuccessors().contains(ctx.exit)
                && !bodyEnd.getSuccessors().contains(postLoop)) {
            bodyEnd.addSuccessor(loopHeader);
            CfgResult.putEdgeLabel(ctx.edgeLabels, bodyEnd, loopHeader, "loop-back");
        }

        return postLoop;
    }

    private BasicBlock walkForEach(ForEachStmt forEachStmt, BasicBlock current, Context ctx) {
        BasicBlock loopHeader = newBlock(ctx);
        current.addSuccessor(loopHeader);
        loopHeader.addStatement("for (" + truncate(forEachStmt.getVariable().toString())
                + " : " + truncate(forEachStmt.getIterable().toString()) + ")");

        BasicBlock postLoop = newBlock(ctx);
        BasicBlock bodyStart = newBlock(ctx);
        loopHeader.addSuccessor(bodyStart);
        CfgResult.putEdgeLabel(ctx.edgeLabels, loopHeader, bodyStart, "true");
        loopHeader.addSuccessor(postLoop);
        CfgResult.putEdgeLabel(ctx.edgeLabels, loopHeader, postLoop, "false");

        Context loopCtx = ctx.withLoop(loopHeader, postLoop);
        BasicBlock bodyEnd = walkStatement(forEachStmt.getBody(), bodyStart, loopCtx);

        if (!bodyEnd.getSuccessors().contains(ctx.exit)
                && !bodyEnd.getSuccessors().contains(postLoop)) {
            bodyEnd.addSuccessor(loopHeader);
            CfgResult.putEdgeLabel(ctx.edgeLabels, bodyEnd, loopHeader, "loop-back");
        }

        return postLoop;
    }

   
    private BasicBlock walkDoWhile(DoStmt doStmt, BasicBlock current, Context ctx) {
        BasicBlock bodyEntry = newBlock(ctx);
        current.addSuccessor(bodyEntry);

        BasicBlock postLoop = newBlock(ctx);
        
        Context loopCtx = ctx.withLoop(null , postLoop);

        BasicBlock bodyEnd = walkStatement(doStmt.getBody(), bodyEntry, loopCtx);

        BasicBlock condBlock = newBlock(ctx);
        if (!bodyEnd.getSuccessors().contains(ctx.exit)
                && !bodyEnd.getSuccessors().contains(postLoop)) {
            bodyEnd.addSuccessor(condBlock);
        }
        condBlock.addStatement("do-while (" + truncate(doStmt.getCondition().toString()) + ")");

     
        condBlock.addSuccessor(bodyEntry);
        CfgResult.putEdgeLabel(ctx.edgeLabels, condBlock, bodyEntry, "true");
        condBlock.addSuccessor(postLoop);
        CfgResult.putEdgeLabel(ctx.edgeLabels, condBlock, postLoop, "false");

        return postLoop;
    }


    private BasicBlock walkReturn(ReturnStmt ret, BasicBlock current, Context ctx) {
        String label = ret.getExpression()
                .map(e -> "return " + truncate(e.toString()))
                .orElse("return");
        current.addStatement(label);
        current.addSuccessor(ctx.exit);

        
        BasicBlock deadBlock = newBlock(ctx);
        return deadBlock;
    }


    private BasicBlock walkBreak(BasicBlock current, Context ctx) {
        current.addStatement("break");
        if (ctx.innerLoopExit != null) {
            current.addSuccessor(ctx.innerLoopExit);
            CfgResult.putEdgeLabel(ctx.edgeLabels, current, ctx.innerLoopExit, "break");
        } else {
            
            current.addSuccessor(ctx.exit);
        }
        return newBlock(ctx);
    }


    private BasicBlock walkContinue(BasicBlock current, Context ctx) {
        current.addStatement("continue");
        if (ctx.innerLoopHeader != null) {
            current.addSuccessor(ctx.innerLoopHeader);
            CfgResult.putEdgeLabel(ctx.edgeLabels, current, ctx.innerLoopHeader, "continue");
        } else {
            current.addSuccessor(ctx.exit);
        }
        return newBlock(ctx);
    }

   
    private BasicBlock newBlock(Context ctx) {
        BasicBlock b = BasicBlock.newNormal();
        ctx.allBlocks.add(b);
        return b;
    }

    
    private String summarise(Statement stmt) {
        String text = stmt.toString().replaceAll("\\s+", " ").trim();
        return truncate(text);
    }


    static String truncate(String s) {
        if (s == null) return "";
        s = s.replaceAll("\\s+", " ").trim();
        return s.length() <= 60 ? s : s.substring(0, 57) + "...";
    }
}
