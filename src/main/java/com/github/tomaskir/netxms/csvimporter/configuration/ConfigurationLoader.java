package com.github.tomaskir.netxms.csvimporter.configuration;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Properties;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ConfigurationLoader {
    @Getter
    private final static ConfigurationLoader instance = new ConfigurationLoader();

    public Properties load(String fileName) throws IOException {
        Properties properties = new Properties();
        FileReader fileReader = new FileReader(Paths.get(fileName).toAbsolutePath().toString());

        properties.load(fileReader);
        fileReader.close();

        return properties;
    }
}
