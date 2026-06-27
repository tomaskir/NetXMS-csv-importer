package com.github.tomaskir.netxms.csvimporter.configuration;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigurationLoaderTest {

    @TempDir
    Path tempDir;

    private String writeProperties(String content) throws IOException {
        Path file = tempDir.resolve("config.properties");
        Files.write(file, content.getBytes());
        return file.toAbsolutePath().toString();
    }

    // --- positive ---

    @Test
    void loadsDeclaredProperties() throws Exception {
        String props = String.join("\n",
                "csv.file.name=nodes.csv",
                "netxms.address=netxms.server.local",
                "netxms.port=4701",
                "import.create.containers=true");

        Properties loaded = ConfigurationLoader.getInstance().load(writeProperties(props));

        assertEquals("nodes.csv", loaded.getProperty("csv.file.name"));
        assertEquals("netxms.server.local", loaded.getProperty("netxms.address"));
        assertEquals("4701", loaded.getProperty("netxms.port"));
        assertEquals("true", loaded.getProperty("import.create.containers"));
    }

    // --- negative ---

    @Test
    void returnsNullForUndeclaredKey() throws Exception {
        Properties loaded = ConfigurationLoader.getInstance().load(writeProperties("csv.file.name=nodes.csv"));

        assertNull(loaded.getProperty("netxms.password"));
    }

    @Test
    void throwsWhenFileMissing() {
        String missing = tempDir.resolve("nope.properties").toAbsolutePath().toString();

        assertThrows(FileNotFoundException.class,
                () -> ConfigurationLoader.getInstance().load(missing));
    }
}
