package com.bytecodecfg.config;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.HashMap;
import java.util.Map;


@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {

    private Map<String, RuleConfig> rules = new HashMap<>();

    public Map<String, RuleConfig> getRules() {
        return rules;
    }

    public void setRules(Map<String, RuleConfig> rules) {
        this.rules = rules != null ? rules : new HashMap<>();
    }

    
    public RuleConfig getRuleConfig(String ruleName) {
        if (rules == null || ruleName == null) {
            return new RuleConfig();
        }
        if (rules.containsKey(ruleName)) {
            return rules.get(ruleName);
        }
        String normalizedTarget = ruleName.endsWith("Rule")
                ? ruleName.substring(0, ruleName.length() - 4)
                : ruleName;

        for (Map.Entry<String, RuleConfig> entry : rules.entrySet()) {
            String key = entry.getKey();
            String normalizedKey = key.endsWith("Rule") ? key.substring(0, key.length() - 4) : key;
            if (key.equalsIgnoreCase(ruleName) || normalizedKey.equalsIgnoreCase(normalizedTarget)) {
                return entry.getValue();
            }
        }
        return new RuleConfig();
    }
}
