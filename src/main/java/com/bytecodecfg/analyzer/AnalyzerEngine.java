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

/**
 * Orchestrates the static analysis pipeline: parse → rule execution → report.
 *
 * <h2>Rule loading</h2>
 * Rules are loaded once at construction time via {@link #loadRules(Config)}.
 * The strategy differs depending on whether a {@link Config} is provided:
 *
 * <ul>
 *   <li><strong>No config ({@code null})</strong> — all three rules are
 *       instantiated with their built-in constructor defaults.  This is the
 *       v1.0 behaviour and must not change.</li>
 *   <li><strong>Config provided</strong> — each rule is only added to the
 *       active list if its {@link RuleConfig#isEnabled()} returns {@code true}.
 *       Additionally, {@link ComplexityRule} respects an optional
 *       {@link RuleConfig#getThreshold()} override; when the override is
 *       {@code null}, the rule falls back to its hardcoded default of 10.</li>
 * </ul>
 *
 * <h2>Configurable constants discovered in rule files</h2>
 * During the YAML-config audit, the following constants were identified as
 * candidates for externalisation:
 * <ul>
 *   <li>{@code ComplexityRule.DEFAULT_THRESHOLD = 10} — <strong>exposed</strong>
 *       via {@code threshold} in {@link com.bytecodecfg.config.RuleConfig}.</li>
 *   <li>{@code NamingRule} patterns ({@code PASCAL_CASE_PATTERN},
 *       {@code CAMEL_CASE_PATTERN}, {@code UPPER_SNAKE_PATTERN}) — intentionally
 *       <strong>not exposed</strong>: they implement the Java Language Specification
 *       and are not user-tunable thresholds.  Exposing them would allow configs
 *       that break standard Java conventions.</li>
 *   <li>{@code NullCheckRule} — contains no numeric thresholds or patterns; the
 *       only config option is {@code enabled}.</li>
 * </ul>
 *
 * @see com.bytecodecfg.config.Config
 * @see com.bytecodecfg.config.ConfigLoader
 */
public class AnalyzerEngine {

    private final String targetPath;
    private final List<Rule> rules;
    private final Reporter reporter;

    /**
     * Convenience constructor using default {@link JsonReporter} and no config.
     * All three rules run at their hardcoded defaults.
     *
     * @param targetPath path to a Java source file or directory to analyse
     */
    public AnalyzerEngine(String targetPath) {
        this(targetPath, new JsonReporter());
    }

    /**
     * Constructor with a custom reporter but no config.
     * All three rules run at their hardcoded defaults.
     *
     * @param targetPath path to a Java source file or directory to analyse
     * @param reporter   the {@link Reporter} implementation used to emit results
     */
    public AnalyzerEngine(String targetPath, Reporter reporter) {
        this(targetPath, reporter, null);
    }

    /**
     * Full constructor supporting an optional YAML-sourced {@link Config}.
     *
     * <p>Passing {@code null} for {@code config} is the same as calling
     * {@link #AnalyzerEngine(String, Reporter)} — all rules run at defaults.
     * This invariant ensures that the {@code --config} flag is purely additive
     * and never changes the tool's output when omitted.</p>
     *
     * @param targetPath path to a Java source file or directory to analyse
     * @param reporter   the {@link Reporter} implementation used to emit results
     * @param config     optional rule configuration; {@code null} means "all rules
     *                   at default settings"
     */
    public AnalyzerEngine(String targetPath, Reporter reporter, Config config) {
        this.targetPath = targetPath;
        this.rules = new ArrayList<>();
        this.reporter = reporter;

        loadRules(config);
    }

    /**
     * Populates {@link #rules} according to the supplied config.
     *
     * <p><strong>No-config path (regression guard):</strong>
     * When {@code config == null}, this method instantiates all three rules
     * with their zero-argument constructors — exactly the pre-config v1.0
     * behaviour.  Nothing in this branch should ever change without explicit
     * approval, because it would silently alter the default output.</p>
     *
     * <p><strong>Config path:</strong>
     * Each rule is looked up by its short name (e.g. {@code "naming"}).
     * {@link Config#getRuleConfig(String)} returns a default {@link RuleConfig}
     * (enabled, no threshold) for any rule not mentioned in the YAML, so omitting
     * a rule key is equivalent to {@code enabled: true}.</p>
     *
     * @param config optional rule configuration; {@code null} uses all-default rules
     */
    private void loadRules(Config config) {
        if (config == null) {
            // ── No-config path ────────────────────────────────────────────────────
            // IMPORTANT: this block must remain byte-for-byte identical to v1.0.
            // Any change here would break the --no-config regression guarantee.
            rules.add(new NamingRule());
            rules.add(new ComplexityRule());     // uses DEFAULT_THRESHOLD = 10
            rules.add(new NullCheckRule());
        } else {
            // ── Config path ───────────────────────────────────────────────────────

            // NamingRule — only enabled/disabled; no additional parameters
            RuleConfig namingCfg = config.getRuleConfig("naming");
            if (namingCfg.isEnabled()) {
                rules.add(new NamingRule());
            }

            // ComplexityRule — supports optional threshold override
            RuleConfig complexityCfg = config.getRuleConfig("complexity");
            if (complexityCfg.isEnabled()) {
                if (complexityCfg.getThreshold() != null) {
                    // User supplied an explicit threshold in YAML
                    rules.add(new ComplexityRule(complexityCfg.getThreshold()));
                } else {
                    // No threshold key in YAML → fall back to rule's own default (10)
                    rules.add(new ComplexityRule());
                }
            }

            // NullCheckRule — only enabled/disabled; no additional parameters
            RuleConfig nullCheckCfg = config.getRuleConfig("nullCheck");
            if (nullCheckCfg.isEnabled()) {
                rules.add(new NullCheckRule());
            }
        }
        System.out.println("Loaded " + rules.size() + " rules.");
    }

    /**
     * Runs the full analysis pipeline: parse all Java files under {@code targetPath},
     * apply each active rule to every {@link CompilationUnit}, collect violations,
     * then delegate formatting and output to the configured {@link Reporter}.
     *
     * <p>Progress messages are written to {@code stdout} so they can be captured
     * or suppressed independently of the report payload by shell redirects.</p>
     */
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

    /**
     * Adds an additional rule to the active rule list.
     * Intended for programmatic use (e.g. unit tests) where a rule needs to be
     * injected after construction.
     *
     * @param rule the {@link Rule} implementation to append
     */
    public void addRule(Rule rule) {
        rules.add(rule);
    }
}
