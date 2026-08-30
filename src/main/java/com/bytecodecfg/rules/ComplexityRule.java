package com.bytecodecfg.rules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.stmt.CatchClause;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.ArrayList;
import java.util.List;


public class ComplexityRule implements Rule {

    private static final String RULE_NAME = "ComplexityRule";
    private static final String DESCRIPTION =
            "Computes cyclomatic complexity for each method and flags methods exceeding defined threshold (default: 10).";
    private static final int DEFAULT_THRESHOLD = 10;

    private final int threshold;

    public ComplexityRule() {
        this(DEFAULT_THRESHOLD);
    }

    public ComplexityRule(int threshold) {
        this.threshold = threshold;
    }

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

        cu.accept(new ComplexityVisitor(violations, fileName, threshold), null);
        return violations;
    }

    private static class ComplexityVisitor extends VoidVisitorAdapter<Void> {

        private final List<String> violations;
        private final String fileName;
        private final int threshold;

        public ComplexityVisitor(List<String> violations, String fileName, int threshold) {
            this.violations = violations;
            this.fileName = fileName;
            this.threshold = threshold;
        }

        @Override
        public void visit(MethodDeclaration n, Void arg) {
            int complexity = calculateComplexity(n);
            if (complexity > threshold) {
                int line = n.getBegin().map(p -> p.line).orElse(-1);
                String methodName = n.getNameAsString();
                violations.add(String.format(
                        "[%s] %s:%d → Method '%s' has complexity of %d, exceeds threshold of %d",
                        RULE_NAME, fileName, line, methodName, complexity, threshold
                ));
            }
            super.visit(n, arg);
        }

        private int calculateComplexity(MethodDeclaration method) {
            int[] complexity = {1}; // Base complexity for method entry

            method.accept(new VoidVisitorAdapter<Void>() {
                @Override
                public void visit(IfStmt n, Void arg) {
                    complexity[0]++;
                    super.visit(n, arg);
                }

                @Override
                public void visit(ForStmt n, Void arg) {
                    complexity[0]++;
                    super.visit(n, arg);
                }

                @Override
                public void visit(ForEachStmt n, Void arg) {
                    complexity[0]++;
                    super.visit(n, arg);
                }

                @Override
                public void visit(WhileStmt n, Void arg) {
                    complexity[0]++;
                    super.visit(n, arg);
                }

                @Override
                public void visit(DoStmt n, Void arg) {
                    complexity[0]++;
                    super.visit(n, arg);
                }

                @Override
                public void visit(CatchClause n, Void arg) {
                    complexity[0]++;
                    super.visit(n, arg);
                }

                @Override
                public void visit(ConditionalExpr n, Void arg) {
                    complexity[0]++;
                    super.visit(n, arg);
                }

                @Override
                public void visit(SwitchEntry n, Void arg) {
                    if (!n.getLabels().isEmpty()) {
                        complexity[0]++;
                    }
                    super.visit(n, arg);
                }

                @Override
                public void visit(BinaryExpr n, Void arg) {
                    if (n.getOperator() == BinaryExpr.Operator.AND ||
                        n.getOperator() == BinaryExpr.Operator.OR) {
                        complexity[0]++;
                    }
                    super.visit(n, arg);
                }
            }, null);

            return complexity[0];
        }
    }
}
