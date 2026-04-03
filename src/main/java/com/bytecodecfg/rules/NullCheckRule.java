package com.bytecodecfg.rules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.NullLiteralExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NullCheckRule implements Rule {

    private static final String RULE_NAME   = "NullCheckRule";
    private static final String DESCRIPTION =
            "Detects potential null dereferences by tracking variables assigned null within methods.";

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

        cu.accept(new NullCheckVisitor(violations, fileName), null);
        return violations;
    }


    private static class NullCheckVisitor extends VoidVisitorAdapter<Void> {

        private final List<String> violations;
        private final String fileName;

        public NullCheckVisitor(List<String> violations, String fileName) {
            this.violations = violations;
            this.fileName   = fileName;
        }


        @Override
        public void visit(MethodDeclaration n, Void arg) {

            Map<String, Integer> nullVariables = new HashMap<>();

            n.accept(new VoidVisitorAdapter<Void>() {


                @Override
                public void visit(VariableDeclarationExpr n, Void arg) {
                    for (VariableDeclarator var : n.getVariables()) {
                        var.getInitializer().ifPresent(init -> {
                            if (init instanceof NullLiteralExpr) {
                                int line = var.getBegin()
                                        .map(p -> p.line)
                                        .orElse(-1);
                                nullVariables.put(var.getNameAsString(), line);
                            }
                        });
                    }
                    super.visit(n, arg);
                }


                @Override
                public void visit(AssignExpr n, Void arg) {
                    if (n.getTarget() instanceof NameExpr) {
                        String varName = ((NameExpr) n.getTarget()).getNameAsString();

                        if (n.getValue() instanceof NullLiteralExpr) {
                            // Variable is being set to null
                            int line = n.getBegin()
                                    .map(p -> p.line)
                                    .orElse(-1);
                            nullVariables.put(varName, line);
                        } else {

                            nullVariables.remove(varName);
                        }
                    }
                    super.visit(n, arg);
                }


                @Override
                public void visit(MethodCallExpr n, Void arg) {
                    n.getScope().ifPresent(scope -> {
                        if (scope instanceof NameExpr) {
                            String varName = ((NameExpr) scope).getNameAsString();

                            if (nullVariables.containsKey(varName)) {
                                int assignedAtLine = nullVariables.get(varName);
                                int line = n.getBegin()
                                        .map(p -> p.line)
                                        .orElse(-1);

                                violations.add(formatViolation(
                                        fileName, line,
                                        String.format(
                                                "Potential null dereference: '%s.%s()' — '%s' was assigned null at line %d",
                                                varName,
                                                n.getNameAsString(),
                                                varName,
                                                assignedAtLine
                                        )
                                ));
                            }
                        }
                    });
                    super.visit(n, arg);
                }

            }, null);

            super.visit(n, arg);
        }


        private String formatViolation(String fileName, int line, String message) {
            return String.format("[%s] %s:%d → %s", RULE_NAME, fileName, line, message);
        }
    }
}