package com.bytecodecfg.rules;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NamingRuleTest {

    private NamingRule rule;

    @BeforeEach
    public void setUp() {
        rule = new NamingRule();
    }

  
    @Test
    public void testNamingRuleViolation() {
        String code = "class invalid_name { void InvalidMethodName() {} }";
        CompilationUnit cu = StaticJavaParser.parse(code);

        List<String> violations = rule.analyze(cu);

        assertFalse(violations.isEmpty(), "Expected naming violations for non-standard class and method names.");
        assertTrue(violations.stream().anyMatch(v -> v.contains("invalid_name")), "Should flag invalid class name.");
        assertTrue(violations.stream().anyMatch(v -> v.contains("InvalidMethodName")), "Should flag invalid method name.");
    }

   
    @Test
    public void testNamingRuleNoViolation() {
        String code = "public class ValidClass { public void validMethod() {} }";
        CompilationUnit cu = StaticJavaParser.parse(code);

        List<String> violations = rule.analyze(cu);

        assertTrue(violations.isEmpty(), "Expected no naming violations for standard Java identifiers.");
    }
}
