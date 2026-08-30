package com.bytecodecfg.rules;

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class NullCheckRuleTest {

    private NullCheckRule rule;

    @BeforeEach
    public void setUp() {
        rule = new NullCheckRule();
    }

   
    @Test
    public void testNullCheckViolation() {
        String code = "public class Test {" +
                "  public void unsafeMethod() {" +
                "    String s = null;" +
                "    s.trim();" +
                "  }" +
                "}";
        CompilationUnit cu = StaticJavaParser.parse(code);

        List<String> violations = rule.analyze(cu);

        assertFalse(violations.isEmpty(), "Expected null check violation for dereferencing null variable.");
        assertTrue(violations.get(0).contains("Potential null dereference"), "Violation should indicate potential null dereference.");
    }

   
    @Test
    public void testNullCheckNoViolation() {
        String code = "public class Test {" +
                "  public void safeMethod() {" +
                "    String s = \"hello\";" +
                "    s.trim();" +
                "  }" +
                "}";
        CompilationUnit cu = StaticJavaParser.parse(code);

        List<String> violations = rule.analyze(cu);

        assertTrue(violations.isEmpty(), "Expected no violations for safe non-null variable usage.");
    }
}
