package com.github.tomaskir.netxms.csvimporter.csv;

import com.github.tomaskir.netxms.csvimporter.exceptions.InvalidCsvDataException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CsvParserTest {

    @TempDir
    Path tempDir;

    private String writeCsv(String content) throws IOException {
        Path file = tempDir.resolve("nodes.csv");
        Files.write(file, content.getBytes());
        return file.toAbsolutePath().toString();
    }

    // --- positive: things that should happen ---

    @Test
    void parsesValidRowsSkippingHeader() throws Exception {
        String csv = String.join("\n",
                "name,address,container",
                "web1,10.0.0.1,DataCenter",
                "web2,10.0.0.2,DataCenter");

        List<CsvNode> nodes = CsvParser.getInstance().parseCsv(writeCsv(csv), ",");

        assertEquals(2, nodes.size());
        assertEquals("web1", nodes.get(0).getName());
        assertEquals("10.0.0.1", nodes.get(0).getAddress());
        assertEquals("DataCenter", nodes.get(0).getContainer());
        assertEquals("web2", nodes.get(1).getName());
    }

    @Test
    void trimsWhitespaceAroundFields() throws Exception {
        String csv = String.join("\n",
                "name,address,container",
                "  web1 ,  10.0.0.1  , DataCenter ");

        List<CsvNode> nodes = CsvParser.getInstance().parseCsv(writeCsv(csv), ",");

        assertEquals(1, nodes.size());
        assertEquals("web1", nodes.get(0).getName());
        assertEquals("10.0.0.1", nodes.get(0).getAddress());
        assertEquals("DataCenter", nodes.get(0).getContainer());
    }

    @Test
    void honoursCustomSeparator() throws Exception {
        String csv = String.join("\n",
                "name;address;container",
                "web1;10.0.0.1;DataCenter");

        List<CsvNode> nodes = CsvParser.getInstance().parseCsv(writeCsv(csv), ";");

        assertEquals(1, nodes.size());
        assertEquals("web1", nodes.get(0).getName());
    }

    @Test
    void returnsEmptyListWhenOnlyHeaderPresent() throws Exception {
        List<CsvNode> nodes = CsvParser.getInstance().parseCsv(writeCsv("name,address,container"), ",");

        assertTrue(nodes.isEmpty());
    }

    // --- negative: things that should NOT happen ---

    @Test
    void rejectsRowWithTooFewFields() throws Exception {
        String csv = String.join("\n",
                "name,address,container",
                "web1,10.0.0.1");

        assertThrows(InvalidCsvDataException.class,
                () -> CsvParser.getInstance().parseCsv(writeCsv(csv), ","));
    }

    @Test
    void rejectsRowWithTooManyFields() throws Exception {
        String csv = String.join("\n",
                "name,address,container",
                "web1,10.0.0.1,DataCenter,extra");

        assertThrows(InvalidCsvDataException.class,
                () -> CsvParser.getInstance().parseCsv(writeCsv(csv), ","));
    }

    @Test
    void rejectsRowWithEmptyField() throws Exception {
        String csv = String.join("\n",
                "name,address,container",
                "web1,,DataCenter");

        assertThrows(InvalidCsvDataException.class,
                () -> CsvParser.getInstance().parseCsv(writeCsv(csv), ","));
    }

    @Test
    void throwsWhenFileMissing() {
        String missing = tempDir.resolve("does-not-exist.csv").toAbsolutePath().toString();

        assertThrows(FileNotFoundException.class,
                () -> CsvParser.getInstance().parseCsv(missing, ","));
    }
}
