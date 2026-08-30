package com.bytecodecfg.reporter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class JsonReporterTest {

    private JsonReporter reporter;

    @BeforeEach
    public void setUp() {
        reporter = new JsonReporter();
    }

    @Test
    public void testReportNonEmptyViolations() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        try {
            System.setOut(new PrintStream(outContent));
            List<String> violations = List.of("[NamingRule] MyClass.java:12 → Method name 'DoThing' should use camelCase");
            reporter.report(violations);

            String jsonOutput = outContent.toString();
            assertNotNull(jsonOutput, "JSON output should not be null.");
            assertTrue(jsonOutput.contains("\"totalViolations\" : 1"), "JSON report should include totalViolations count of 1.");
            assertTrue(jsonOutput.contains("\"rule\" : \"NamingRule\""), "JSON report should include rule name.");
            assertTrue(jsonOutput.contains("\"file\" : \"MyClass.java\""), "JSON report should include file path.");
            assertTrue(jsonOutput.contains("\"line\" : 12"), "JSON report should include line number.");
            assertTrue(jsonOutput.contains("\"message\" : \"Method name 'DoThing' should use camelCase\""), "JSON report should include message.");
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

            String jsonOutput = outContent.toString();
            assertNotNull(jsonOutput, "JSON output should not be null.");
            assertTrue(jsonOutput.contains("\"totalViolations\" : 0"), "JSON output should state totalViolations of 0.");
            assertTrue(jsonOutput.contains("\"violations\" : [ ]"), "JSON output should contain empty violations array.");
        } finally {
            System.setOut(originalOut);
        }
    }
}
