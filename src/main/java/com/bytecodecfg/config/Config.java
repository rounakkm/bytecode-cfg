package com.bytecodecfg.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Top-level configuration object deserialized from a {@code bytecodecfg.yml} file.
 *
 * <h2>Full YAML schema</h2>
 * <pre>{@code
 * # bytecodecfg.yml — BytecodeCFG rule configuration
 * #
 * # All rule entries are optional; omitting a rule is equivalent to:
 * #   enabled: true  (rule runs with its own hardcoded defaults)
 * #
 * rules:
 *   naming:
 *     enabled: true   # set to false to silence all NamingRule violations
 *
 *   complexity:
 *     enabled: true
 *     threshold: 10   # methods with cyclomatic complexity > threshold are flagged
 *                     # default is 10 when omitted
 *
 *   nullCheck:
 *     enabled: true   # set to false to silence all NullCheckRule violations
 * }</pre>
 *
 * <h2>Rule key names</h2>
 * Keys are matched case-insensitively and with/without the trailing "Rule" suffix,
 * so {@code naming}, {@code Naming}, and {@code NamingRule} all resolve to the
 * same {@link RuleConfig}.  See {@link #getRuleConfig(String)} for the exact
 * matching algorithm.
 *
 * <h2>No-config default behaviour</h2>
 * When the user omits {@code --config}, {@code Main} passes {@code null} to
 * {@link com.bytecodecfg.analyzer.AnalyzerEngine}, which then uses all three
 * rules with their built-in defaults — identical to v1.0 behaviour before this
 * feature was added.
 *
 * @see RuleConfig
 * @see ConfigLoader
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {

    /**
     * Map from rule name (as it appears in the YAML, e.g. {@code "naming"})
     * to its {@link RuleConfig}.  Populated by Jackson during deserialization.
     * Never {@code null} after construction (guaranteed by {@link #setRules}).
     */
    private Map<String, RuleConfig> rules = new HashMap<>();

    /**
     * Returns the raw rules map.
     * Prefer {@link #getRuleConfig(String)} for safe lookup with fallback.
     *
     * @return mutable map from YAML rule-key to {@link RuleConfig}
     */
    public Map<String, RuleConfig> getRules() {
        return rules;
    }

    /**
     * Called by Jackson to inject the deserialized rules map.
     * A {@code null} argument (e.g. if the YAML has an empty {@code rules:} block)
     * is silently replaced with an empty map so that downstream code never
     * needs to null-check the map itself.
     *
     * @param rules deserialized map, may be {@code null}
     */
    public void setRules(Map<String, RuleConfig> rules) {
        this.rules = rules != null ? rules : new HashMap<>();
    }

    /**
     * Looks up the {@link RuleConfig} for a given rule name, with
     * case-insensitive and suffix-tolerant matching.
     *
     * <p>Matching order:</p>
     * <ol>
     *   <li>Exact key match (e.g. {@code "complexity"} → {@code "complexity"})</li>
     *   <li>Case-insensitive match or match after stripping the {@code "Rule"} suffix
     *       from both the lookup name and the YAML key
     *       (e.g. {@code "ComplexityRule"} normalises to {@code "Complexity"},
     *        then matches YAML key {@code "complexity"} case-insensitively)</li>
     *   <li>If no match is found, returns a fresh {@link RuleConfig} with defaults
     *       ({@code enabled=true}, {@code threshold=null}) — so a rule not
     *       mentioned in the YAML still runs at its built-in defaults.</li>
     * </ol>
     *
     * @param ruleName rule identifier (e.g. {@code "naming"}, {@code "NamingRule"})
     * @return the matching {@link RuleConfig}, or a default instance if absent
     */
    public RuleConfig getRuleConfig(String ruleName) {
        if (rules == null || ruleName == null) {
            return new RuleConfig();
        }
        // 1) Exact match
        if (rules.containsKey(ruleName)) {
            return rules.get(ruleName);
        }
        // 2) Strip "Rule" suffix from lookup name for normalised comparison
        String normalizedTarget = ruleName.endsWith("Rule")
                ? ruleName.substring(0, ruleName.length() - 4)
                : ruleName;

        for (Map.Entry<String, RuleConfig> entry : rules.entrySet()) {
            String key = entry.getKey();
            // Also strip "Rule" suffix from the YAML key if present
            String normalizedKey = key.endsWith("Rule") ? key.substring(0, key.length() - 4) : key;
            if (key.equalsIgnoreCase(ruleName) || normalizedKey.equalsIgnoreCase(normalizedTarget)) {
                return entry.getValue();
            }
        }
        // 3) Not found — return defaults (rule enabled, no threshold override)
        return new RuleConfig();
    }
}
