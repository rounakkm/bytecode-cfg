package com.bytecodecfg.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Loads and validates a {@link Config} from a YAML file on disk.
 *
 * <h2>Dependency: jackson-dataformat-yaml</h2>
 * YAML parsing is performed by {@code com.fasterxml.jackson.dataformat:jackson-dataformat-yaml},
 * which itself delegates to SnakeYAML under the hood.  The dependency is declared in
 * {@code pom.xml} as:
 * <pre>{@code
 * <dependency>
 *   <groupId>com.fasterxml.jackson.dataformat</groupId>
 *   <artifactId>jackson-dataformat-yaml</artifactId>
 *   <version>2.16.1</version>
 * </dependency>
 * }</pre>
 * {@code jackson-databind} (already present for {@code JsonReporter}) is reused here —
 * no additional runtime cost beyond the YAML format module.
 *
 * <h2>Error handling contract</h2>
 * This class <em>never silently falls back to defaults</em> on bad input.
 * Callers (i.e. {@code Main}) must treat every thrown exception as a fatal
 * configuration error and exit with a non-zero status.  Rationale: a silent
 * fallback would hide user mistakes in the config file.
 * <ul>
 *   <li>{@link IllegalArgumentException} — null/empty path, or path points to a
 *       directory rather than a file</li>
 *   <li>{@link FileNotFoundException} — path does not exist</li>
 *   <li>{@link IOException} — YAML is syntactically malformed or Jackson cannot
 *       map it to {@link Config}</li>
 * </ul>
 *
 * @see Config
 * @see RuleConfig
 */
public class ConfigLoader {

    /**
     * Thread-safe, reusable Jackson {@link ObjectMapper} configured to parse YAML.
     * {@link ObjectMapper} instances are expensive to construct and are safe to
     * share across threads once configured, so we keep a single static instance.
     */
    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());

    /**
     * Reads and deserializes the YAML config file at {@code configPath} into a
     * {@link Config} instance.
     *
     * <p><strong>No silent fallback:</strong> every error condition throws an
     * exception so that callers can surface a clear error to the user.
     *
     * @param configPath absolute or relative path to a {@code .yml} / {@code .yaml}
     *                   config file (e.g. {@code "demo/bytecodecfg.yml"})
     * @return a fully populated {@link Config}; never {@code null}
     * @throws IllegalArgumentException if {@code configPath} is null, blank,
     *                                  or refers to a directory
     * @throws FileNotFoundException    if the file does not exist at the given path
     * @throws IOException              if the file exists but cannot be parsed
     *                                  (malformed YAML or unexpected structure)
     */
    public static Config loadConfig(String configPath) throws IOException {
        if (configPath == null || configPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Config path cannot be null or empty.");
        }

        File configFile = new File(configPath);
        if (!configFile.exists()) {
            // Throw FileNotFoundException (subtype of IOException) so callers can
            // distinguish "file not found" from "file found but malformed".
            throw new FileNotFoundException("Config file does not exist: " + configPath);
        }
        if (!configFile.isFile()) {
            throw new IllegalArgumentException("Config path is not a file: " + configPath);
        }

        try {
            // Jackson maps the YAML to Config using the annotated field names.
            // Unknown YAML keys are silently ignored (@JsonIgnoreProperties on Config/RuleConfig).
            Config config = YAML_MAPPER.readValue(configFile, Config.class);
            // Guard against an edge case where Jackson returns null for a completely empty file.
            return config != null ? config : new Config();
        } catch (Exception e) {
            // Wrap any Jackson/SnakeYAML parse exception in IOException so it propagates
            // as a checked exception through the public API.
            throw new IOException(
                    "Failed to parse YAML configuration file '" + configPath + "': " + e.getMessage(), e);
        }
    }
}
