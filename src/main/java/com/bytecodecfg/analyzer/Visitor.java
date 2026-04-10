package com.bytecodecfg.analyzer;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

public class Visitor {
    public static class AstItem {

        private final String type;
        private final String name;
        private final int line;

        public AstItem(String type, String name, int line) {
            this.type = type;
            this.name = name;
            this.line = line;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public int getLine() {
            return line;
        }

        @Override
        public String toString() {
            return String.format("[%s] %s (line %d)", type, name, line);
        }
    }


    public List<AstItem> extractItems(CompilationUnit cu) {
        List<AstItem> items = new ArrayList<>();
        cu.accept(new StructureVisitor(items), null);
        return items;
    }


    private static class StructureVisitor extends VoidVisitorAdapter<Void> {

        private final List<AstItem> items;

        public StructureVisitor(List<AstItem> items) {
            this.items = items;
        }

  
        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
            String type = n.isInterface() ? "Interface" : "Class";
            int line    = n.getBegin().map(p -> p.line).orElse(-1);

            items.add(new AstItem(type, n.getNameAsString(), line));
            super.visit(n, arg);
        }


        @Override
        public void visit(MethodDeclaration n, Void arg) {
            int line = n.getBegin().map(p -> p.line).orElse(-1);

            items.add(new AstItem(
                    "Method",
                    n.getNameAsString(),
                    line
            ));
            super.visit(n, arg);
        }


        @Override
        public void visit(FieldDeclaration n, Void arg) {
            for (VariableDeclarator var : n.getVariables()) {
                int line = var.getBegin().map(p -> p.line).orElse(-1);

                items.add(new AstItem(
                        "Field",
                        var.getNameAsString(),
                        line
                ));
            }
            super.visit(n, arg);
        }
    }
}
