package com.bytecodecfg.reporter;

import java.util.List;

/**
 * Reporter is the base interface that every report output format must implement.
 *
 * Each reporter is responsible for:
 * - Taking the list of violations collected by AnalyzerEngine
 * - Formatting and outputting them in a specific format (JSON, HTML, etc.)
 *
 * To add a new output format to BytecodeCFG:
 * 1. Create a new class implementing this interface
 * 2. Implement report() with your desired output format
 * 3. Pass an instance of it to AnalyzerEngine
 *
 * Current implementations:
 * - JsonReporter  → outputs violations as a JSON report to terminal
 * - HtmlReporter  → outputs violations as a formatted HTML file
 */
public interface Reporter {

    /**
     * Formats and outputs the given list of violations.
     *
     * Implementations should:
     * - Handle an empty list gracefully (print a "no violations found" message)
     * - Never throw unchecked exceptions for formatting errors
     * - Always produce some output even if the violations list is empty
     *
     * @param violations a non-null list of violation message strings
     *                   collected from all rules across all analyzed files
     */
    void report(List<String> violations);
}