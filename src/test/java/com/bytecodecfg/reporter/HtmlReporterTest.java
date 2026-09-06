package com.bytecodecfg.reporter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HtmlReporterTest {

    private HtmlReporter reporter;

    @BeforeEach
    public void setUp() {
        reporter = new HtmlReporter();
    }

    @Test
    public void testReportNonEmptyViolations() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(outContent));
            List<String> violations = List.of("[NamingRule] MyClass.java:12 → Method name 'DoThing' should use camelCase");
            reporter.report(violations);

            String htmlOutput = outContent.toString();
            assertNotNull(htmlOutput, "HTML output should not be null.");
            assertTrue(htmlOutput.contains("<!doctype html>"), "HTML output should start with doctype declaration.");
            assertTrue(htmlOutput.contains("Total violations:</strong> 1"), "HTML report should state total violations of 1.");
            assertTrue(htmlOutput.contains("NamingRule"), "HTML report should include rule name.");
            assertTrue(htmlOutput.contains("MyClass.java"), "HTML report should include file path.");
            assertTrue(htmlOutput.contains("12"), "HTML report should include line number.");
            assertTrue(htmlOutput.contains("Method name &#39;DoThing&#39; should use camelCase"), "HTML report should escape and include message.");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testReportEmptyViolations() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(outContent));
            reporter.report(Collections.emptyList());

            String htmlOutput = outContent.toString();
            assertNotNull(htmlOutput, "HTML output should not be null.");
            assertTrue(htmlOutput.contains("Total violations:</strong> 0"), "HTML report should state total violations of 0.");
            assertTrue(htmlOutput.contains("No violations found."), "HTML output should contain 'No violations found.' message.");
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    public void testReportWithAnalyzedTarget() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(outContent));
            HtmlReporter targetReporter = new HtmlReporter("demo/Sample.java");
            targetReporter.report(Collections.emptyList());

            String htmlOutput = outContent.toString();
            assertNotNull(htmlOutput, "HTML output should not be null.");
            assertTrue(htmlOutput.contains("demo/Sample.java"), "HTML report should include analyzed target path.");
        } finally {
            System.setOut(originalOut);
        }
    }
}
