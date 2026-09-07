package com.bytecodecfg.reporter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HtmlReporter implements Reporter {

    private static final Pattern VIOLATION_PATTERN =
            Pattern.compile("^\\[([^\\]]+)\\]\\s+([^:]+):(\\d+)\\s+→\\s+(.*)$");
    private final String analyzedTarget;

    public HtmlReporter() {
        this(null);
    }
   
    public HtmlReporter(String analyzedTarget) {
        this.analyzedTarget = analyzedTarget;
    }

    @Override
    public void report(List<String> violations) {
        List<ViolationEntry> entries = parseViolations(violations);
        System.out.println(buildHtml(entries));
    }

    private List<ViolationEntry> parseViolations(List<String> violations) {
        List<String> safeViolations = violations == null ? Collections.emptyList() : violations;
        List<ViolationEntry> entries = new ArrayList<>();

        for (String rawViolation : safeViolations) {
            if (rawViolation != null && !rawViolation.trim().isEmpty()) {
                entries.add(parseViolation(rawViolation));
            }
        }
        return entries;
    }

    private ViolationEntry parseViolation(String rawViolation) {
        Matcher matcher = VIOLATION_PATTERN.matcher(rawViolation.trim());
        if (matcher.matches()) {
            return new ViolationEntry(
                    matcher.group(1).trim(),
                    matcher.group(2).trim(),
                    Integer.parseInt(matcher.group(3)),
                    matcher.group(4).trim());
        }

        return new ViolationEntry("UnknownRule", "UnknownFile", -1, rawViolation);
    }

    private String buildHtml(List<ViolationEntry> entries) {
        Set<String> files = new TreeSet<>();
        for (ViolationEntry entry : entries) {
            if (!"UnknownFile".equals(entry.file)) {
                files.add(entry.file);
            }
        }
        String fileSummary = analyzedTarget == null || analyzedTarget.trim().isEmpty()
                ? (files.isEmpty() ? "no source files listed" : String.join(", ", files))
                : analyzedTarget;
        StringBuilder html = new StringBuilder();
        html.append("<!doctype html>\n<html lang=\"en\">\n<head>\n")
                .append("  <meta charset=\"UTF-8\">\n")
                .append("  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n")
                .append("  <title>BytecodeCFG Analysis Report</title>\n")
                .append("  <style>\n")
                .append("    body { font-family: Arial, sans-serif; margin: 2rem; color: #222; }\n")
                .append("    table { border-collapse: collapse; width: 100%; margin-top: 1rem; }\n")
                .append("    th, td { border: 1px solid #bbb; padding: 0.55rem; text-align: left; vertical-align: top; }\n")
                .append("    th { background: #f0f0f0; }\n")
                .append("    tbody tr:nth-child(even) { background: #fafafa; }\n")
                .append("    .empty { padding: 1rem; background: #f6f6f6; border: 1px solid #ccc; }\n")
                .append("  </style>\n</head>\n<body>\n")
                .append("  <h1>BytecodeCFG Analysis Report</h1>\n")
                .append("  <p><strong>Total violations:</strong> ").append(entries.size())
                .append("<br><strong>Files analyzed:</strong> ").append(escapeHtml(fileSummary)).append("</p>\n");

        if (entries.isEmpty()) {
            html.append("  <p class=\"empty\">No violations found.</p>\n");
        } else {
            html.append("  <table>\n    <thead><tr><th>Rule name</th><th>File</th><th>Line number</th><th>Message</th></tr></thead>\n    <tbody>\n");
            for (ViolationEntry entry : entries) {
                html.append("      <tr><td>").append(escapeHtml(entry.rule))
                        .append("</td><td>").append(escapeHtml(entry.file))
                        .append("</td><td>").append(entry.line)
                        .append("</td><td>").append(escapeHtml(entry.message))
                        .append("</td></tr>\n");
            }
            html.append("    </tbody>\n  </table>\n");
        }

        return html.append("</body>\n</html>").toString();
    }

    private String escapeHtml(String value) {
        return value.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private static final class ViolationEntry {
        private final String rule;
        private final String file;
        private final int line;
        private final String message;

        private ViolationEntry(String rule, String file, int line, String message) {
            this.rule = rule;
            this.file = file;
            this.line = line;
            this.message = message;
        }
    }
}
