package com.bytecodecfg.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class ConfigLoaderTest {

    @TempDir
    Path tempDir;

    @Test
    public void testLoadValidConfig() throws IOException {
        String yamlContent = "rules:\n" +
                "  naming:\n" +
                "    enabled: false\n" +
                "  complexity:\n" +
                "    enabled: true\n" +
                "    threshold: 5\n" +
                "  nullCheck:\n" +
                "    enabled: true\n";

        Path yamlFile = tempDir.resolve("bytecodecfg.yml");
        Files.writeString(yamlFile, yamlContent);

        Config config = ConfigLoader.loadConfig(yamlFile.toString());
        assertNotNull(config);

        RuleConfig namingConfig = config.getRuleConfig("naming");
        assertFalse(namingConfig.isEnabled());

        RuleConfig complexityConfig = config.getRuleConfig("complexity");
        assertTrue(complexityConfig.isEnabled());
        assertEquals(5, complexityConfig.getThreshold());

        RuleConfig nullCheckConfig = config.getRuleConfig("nullCheck");
        assertTrue(nullCheckConfig.isEnabled());
    }

    @Test
    public void testLoadConfigMissingFile() {
        String nonExistentPath = tempDir.resolve("missing.yml").toString();
        assertThrows(FileNotFoundException.class, () -> ConfigLoader.loadConfig(nonExistentPath));
    }

    @Test
    public void testLoadMalformedYaml() throws IOException {
        String malformedContent = "rules:\n  naming: [unclosed_list";
        Path malformedFile = tempDir.resolve("malformed.yml");
        Files.writeString(malformedFile, malformedContent);

        assertThrows(IOException.class, () -> ConfigLoader.loadConfig(malformedFile.toString()));
    }
}
