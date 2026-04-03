package com.bytecodecfg.rules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;

public class NamingRule implements Rule {

    private static final String RULE_NAME = "NamingRule";
    private static final String DESCRIPTION =
            "Enforces Java naming conventions for classes, methods, fields, and constants.";


    private static final String PASCAL_CASE_PATTERN = "^[A-Z][a-zA-Z0-9]*$";
    private static final String CAMEL_CASE_PATTERN  = "^[a-z][a-zA-Z0-9]*$";
    private static final String UPPER_SNAKE_PATTERN = "^[A-Z][A-Z0-9_]*$";

    @Override
    public String getName() {
        return RULE_NAME;
    }

    @Override
    public String getDescription() {
        return DESCRIPTION;
    }


    @Override
    public List<String> analyze(CompilationUnit cu) {
        List<String> violations = new ArrayList<>();
        String fileName = cu.getStorage()
                .map(s -> s.getFileName())
                .orElse("Unknown file");

        cu.accept(new NamingVisitor(violations, fileName), null);
        return violations;
    }


    private static class NamingVisitor extends VoidVisitorAdapter<Void> {

        private final List<String> violations;
        private final String fileName;

        public NamingVisitor(List<String> violations, String fileName) {
            this.violations = violations;
            this.fileName = fileName;
        }


        @Override
        public void visit(ClassOrInterfaceDeclaration n, Void arg) {
            String name = n.getNameAsString();
            int line = n.getBegin().map(p -> p.line).orElse(-1);

            if (!name.matches(PASCAL_CASE_PATTERN)) {
                violations.add(formatViolation(
                        fileName, line,
                        String.format(
                                "%s name '%s' should use PascalCase (e.g. '%s')",
                                n.isInterface() ? "Interface" : "Class",
                                name,
                                toPascalCase(name)
                        )
                ));
            }


            super.visit(n, arg);
        }


        @Override
        public void visit(MethodDeclaration n, Void arg) {
            String name = n.getNameAsString();
            int line = n.getBegin().map(p -> p.line).orElse(-1);

            if (!name.matches(CAMEL_CASE_PATTERN)) {
                violations.add(formatViolation(
                        fileName, line,
                        String.format(
                                "Method name '%s' should use camelCase (e.g. '%s')",
                                name,
                                toCamelCase(name)
                        )
                ));
            }

            super.visit(n, arg);
        }


        @Override
        public void visit(FieldDeclaration n, Void arg) {
            boolean isConstant = n.isStatic() && n.isFinal();

            for (VariableDeclarator var : n.getVariables()) {
                String name = var.getNameAsString();
                int line = var.getBegin().map(p -> p.line).orElse(-1);

                if (isConstant) {
                    if (!name.matches(UPPER_SNAKE_PATTERN)) {
                        violations.add(formatViolation(
                                fileName, line,
                                String.format(
                                        "Constant '%s' should use UPPER_SNAKE_CASE (e.g. '%s')",
                                        name,
                                        toUpperSnakeCase(name)
                                )
                        ));
                    }
                } else {
                    if (!name.matches(CAMEL_CASE_PATTERN)) {
                        violations.add(formatViolation(
                                fileName, line,
                                String.format(
                                        "Field name '%s' should use camelCase (e.g. '%s')",
                                        name,
                                        toCamelCase(name)
                                )
                        ));
                    }
                }
            }

            super.visit(n, arg);
        }




        private String formatViolation(String fileName, int line, String message) {
            return String.format("[%s] %s:%d → %s", RULE_NAME, fileName, line, message);
        }

        private String toPascalCase(String name) {
            if (name == null || name.isEmpty()) return name;
            return Character.toUpperCase(name.charAt(0)) + name.substring(1);
        }


        private String toCamelCase(String name) {
            if (name == null || name.isEmpty()) return name;
            return Character.toLowerCase(name.charAt(0)) + name.substring(1);
        }


        private String toUpperSnakeCase(String name) {
            if (name == null || name.isEmpty()) return name;
            return name.replaceAll("([a-z])([A-Z])", "$1_$2").toUpperCase();
        }
    }
}