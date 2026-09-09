package com.bytecodecfg.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Per-rule configuration block that can appear under the {@code rules} map
 * in the {@code bytecodecfg.yml} config file.
 *
 * <h2>YAML schema for a single rule entry</h2>
 * <pre>{@code
 * rules:
 *   naming:
 *     enabled: false          # boolean, default true
 *   complexity:
 *     enabled: true
 *     threshold: 5            # int, overrides ComplexityRule default of 10
 *   nullCheck:
 *     enabled: true
 * }</pre>
 *
 * <h2>Fields</h2>
 * <ul>
 *   <li>{@code enabled} — whether the rule should run at all.
 *       Defaults to {@code true} so that omitting a rule from the YAML
 *       has the same effect as leaving it enabled.</li>
 *   <li>{@code threshold} — only meaningful for {@code ComplexityRule};
 *       ignored by the other two rules. When {@code null} (not specified),
 *       {@link com.bytecodecfg.rules.ComplexityRule} falls back to its own
 *       hardcoded default of {@code 10}.</li>
 * </ul>
 *
 * <h2>Design rationale</h2>
 * Using {@code Integer} (boxed) rather than {@code int} for {@code threshold}
 * lets us distinguish "user explicitly set 0" from "user omitted the field".
 * A {@code null} threshold means "use the rule's own default".
 *
 * {@code @JsonIgnoreProperties(ignoreUnknown = true)} allows forward-compatible
 * YAML files: extra keys added in future versions are silently tolerated
 * rather than causing a parse error.
 *
 * @see Config
 * @see ConfigLoader
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class RuleConfig {

    /**
     * Whether this rule is active.
     * Default: {@code true} — a rule that has no entry in the YAML
     * (and therefore gets a fresh {@code RuleConfig()} from
     * {@link Config#getRuleConfig(String)}) will still run.
     */
    private boolean enabled = true;

    /**
     * Optional complexity threshold override.
     * {@code null} means "use the rule's built-in default" (currently 10).
     * Only used by {@link com.bytecodecfg.rules.ComplexityRule}; other rules
     * ignore this field.
     */
    private Integer threshold;

    /**
     * Returns {@code true} if this rule should be executed during analysis.
     *
     * @return {@code true} if enabled (default), {@code false} if explicitly
     *         disabled in the YAML config
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether this rule should run.
     * Jackson calls this via reflection when deserializing the YAML field
     * {@code enabled: false}.
     *
     * @param enabled {@code false} to skip this rule entirely
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the optional complexity threshold, or {@code null} if not set.
     * When {@code null}, {@link com.bytecodecfg.rules.ComplexityRule} uses
     * its own hardcoded default ({@code 10}).
     *
     * @return threshold value, or {@code null} if not specified in YAML
     */
    public Integer getThreshold() {
        return threshold;
    }

    /**
     * Sets the complexity threshold.
     * Jackson calls this via reflection when deserializing the YAML field
     * {@code threshold: 5}.
     *
     * @param threshold positive integer to override the rule's default;
     *                  {@code null} to use the rule's built-in default
     */
    public void setThreshold(Integer threshold) {
        this.threshold = threshold;
    }
}
