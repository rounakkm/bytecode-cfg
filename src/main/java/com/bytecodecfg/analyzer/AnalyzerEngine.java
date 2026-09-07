package com.bytecodecfg.analyzer;

import com.bytecodecfg.config.Config;
import com.bytecodecfg.config.RuleConfig;
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
        this(targetPath, new JsonReporter());
    }

    public AnalyzerEngine(String targetPath, Reporter reporter) {
        this(targetPath, reporter, null);
    }

    public AnalyzerEngine(String targetPath, Reporter reporter, Config config) {
        this.targetPath = targetPath;
        this.rules = new ArrayList<>();
        this.reporter = reporter;

        loadRules(config);
    }

    private void loadRules(Config config) {
        if (config == null) {
            rules.add(new NamingRule());
            rules.add(new ComplexityRule());
            rules.add(new NullCheckRule());
        } else {
            RuleConfig namingCfg = config.getRuleConfig("naming");
            if (namingCfg.isEnabled()) {
                rules.add(new NamingRule());
            }

            RuleConfig complexityCfg = config.getRuleConfig("complexity");
            if (complexityCfg.isEnabled()) {
                if (complexityCfg.getThreshold() != null) {
                    rules.add(new ComplexityRule(complexityCfg.getThreshold()));
                } else {
                    rules.add(new ComplexityRule());
                }
            }

            RuleConfig nullCheckCfg = config.getRuleConfig("nullCheck");
            if (nullCheckCfg.isEnabled()) {
                rules.add(new NullCheckRule());
            }
        }
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

       
        System.out.println("Analysis complete. Found " + allViolations.size() + " violation(s).\n");
        reporter.report(allViolations);
    }


    public void addRule(Rule rule) {
        rules.add(rule);
    }
}
