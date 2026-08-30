package com.bytecodecfg.reporter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class JsonReporter implements Reporter {

   
    private static final Pattern VIOLATION_PATTERN =
            Pattern.compile("^\\[([^\\]]+)\\]\\s+([^:]+):(\\d+)\\s+→\\s+(.*)$");

    private final ObjectMapper objectMapper;

    
    public JsonReporter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

   
    @Override
    public void report(List<String> violations) {
        List<String> safeViolations = (violations == null) ? Collections.emptyList() : violations;

        List<ViolationEntry> entries = new ArrayList<>();
        for (String rawViolation : safeViolations) {
            if (rawViolation != null && !rawViolation.trim().isEmpty()) {
                entries.add(parseViolation(rawViolation));
            }
        }

        ReportOutput output = new ReportOutput(entries.size(), entries);

        try {
            String jsonOutput = objectMapper.writeValueAsString(output);
            System.out.println(jsonOutput);
        } catch (Exception e) {
           
            System.err.println("Error generating JSON report: " + e.getMessage());
        }
    }

 
    private ViolationEntry parseViolation(String rawViolation) {
        Matcher matcher = VIOLATION_PATTERN.matcher(rawViolation.trim());
        if (matcher.matches()) {
            String rule = matcher.group(1).trim();
            String file = matcher.group(2).trim();
            int line = Integer.parseInt(matcher.group(3));
            String message = matcher.group(4).trim();
            return new ViolationEntry(rule, file, line, message);
        }

       
        return new ViolationEntry("UnknownRule", "UnknownFile", -1, rawViolation);
    }

    public static class ReportOutput {
        private final int totalViolations;
        private final List<ViolationEntry> violations;

        public ReportOutput(int totalViolations, List<ViolationEntry> violations) {
            this.totalViolations = totalViolations;
            this.violations = violations;
        }

        public int getTotalViolations() {
            return totalViolations;
        }

        public List<ViolationEntry> getViolations() {
            return violations;
        }
    }

    
    public static class ViolationEntry {
        private final String rule;
        private final String file;
        private final int line;
        private final String message;

        public ViolationEntry(String rule, String file, int line, String message) {
            this.rule = rule;
            this.file = file;
            this.line = line;
            this.message = message;
        }

        public String getRule() {
            return rule;
        }

        public String getFile() {
            return file;
        }

        public int getLine() {
            return line;
        }

        public String getMessage() {
            return message;
        }
    }
}
