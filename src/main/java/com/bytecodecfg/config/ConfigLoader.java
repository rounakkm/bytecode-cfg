package com.bytecodecfg.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;


public class ConfigLoader {

    private static final ObjectMapper YAML_MAPPER = new ObjectMapper(new YAMLFactory());


    public static Config loadConfig(String configPath) throws IOException {
        if (configPath == null || configPath.trim().isEmpty()) {
            throw new IllegalArgumentException("Config path cannot be null or empty.");
        }

        File configFile = new File(configPath);
        if (!configFile.exists()) {
            throw new FileNotFoundException("Config file does not exist: " + configPath);
        }
        if (!configFile.isFile()) {
            throw new IllegalArgumentException("Config path is not a file: " + configPath);
        }

        try {
            Config config = YAML_MAPPER.readValue(configFile, Config.class);
            return config != null ? config : new Config();
        } catch (Exception e) {
            throw new IOException("Failed to parse YAML configuration file '" + configPath + "': " + e.getMessage(), e);
        }
    }
}
