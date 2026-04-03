package com.bytecodecfg.analyzer;

import com.bytecodecfg.reporter.JsonReporter;
import com.bytecodecfg.reporter.Reporter;
import com.bytecodecfg.rules.ComplexityRule;
import com.bytecodecfg.rules.NamingRule;
import com.bytecodecfg.rules.NullCheckRule;
import com.bytecodecfg.rules.Rule;
import com.github.javaparser.ast.CompilationUnit;

import java.util.ArrayList;
import java.util.List;

public class AnalyzerEngine {

    private final String targetPath;
    private final List<Rule> rules;
    private final Reporter reporter;

    public AnalyzerEngine(String targetPath) {
        this.targetPath = targetPath;
        this.rules = new ArrayList<>();
        this.reporter = new JsonReporter();

        loadDefaultRules();
    }


    private void loadDefaultRules() {
        rules.add(new NamingRule());
        rules.add(new ComplexityRule());
        rules.add(new NullCheckRule());
        System.out.println("Loaded " + rules.size() + " rules.");
    }


    public void run() {

        Parser parser = new Parser(targetPath);
        List<CompilationUnit> compilationUnits = parser.parse();

        if (compilationUnits.isEmpty()) {
            System.out.println("No Java files found to analyze.");
            return;
        }

        System.out.println("Analyzing " + compilationUnits.size() + " file(s)...\n");


        List<String> allViolations = new ArrayList<>();

        for (CompilationUnit cu : compilationUnits) {
            for (Rule rule : rules) {
                List<String> violations = rule.analyze(cu);
                allViolations.addAll(violations);
            }
        }

        //results
        System.out.println("Analysis complete. Found " + allViolations.size() + " violation(s).\n");
        reporter.report(allViolations);
    }


    public void addRule(Rule rule) {
        rules.add(rule);
    }
}