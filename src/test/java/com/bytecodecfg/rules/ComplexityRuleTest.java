package com.bytecodecfg.rules;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class ComplexityRuleTest {

    private ComplexityRule rule;

    @BeforeEach
    public void setUp() {
        rule = new ComplexityRule();
    }

   
    @Test
    public void testComplexityViolation() {
        String code = "public class Test {" +
                "  public void complexMethod(int x) {" +
                "    if (x == 1) {}" +
                "    if (x == 2) {}" +
                "    if (x == 3) {}" +
                "    if (x == 4) {}" +
                "    if (x == 5) {}" +
                "    if (x == 6) {}" +
                "    if (x == 7) {}" +
                "    if (x == 8) {}" +
                "    if (x == 9) {}" +
                "    if (x == 10) {}" +
                "    if (x == 11) {}" +
                "  }" +
                "}";
        CompilationUnit cu = StaticJavaParser.parse(code);

        List<String> violations = rule.analyze(cu);

        assertFalse(violations.isEmpty(), "Expected violation for method exceeding complexity threshold of 10.");
        assertTrue(violations.get(0).contains("complexMethod"), "Violation message should mention the method name.");
    }

   
    @Test
    public void testComplexityNoViolation() {
        String code = "public class Test {" +
                "  public void simpleMethod(int x) {" +
                "    if (x > 0) {" +
                "      System.out.println(x);" +
                "    }" +
                "  }" +
                "}";
        CompilationUnit cu = StaticJavaParser.parse(code);

        List<String> violations = rule.analyze(cu);

        assertTrue(violations.isEmpty(), "Expected no violation for simple method with low complexity.");
    }
}
