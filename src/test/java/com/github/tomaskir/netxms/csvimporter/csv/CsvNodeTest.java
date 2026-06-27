package com.github.tomaskir.netxms.csvimporter.csv;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvNodeTest {

    @Test
    void gettersReturnConstructorValues() {
        CsvNode node = new CsvNode("web1", "10.0.0.1", "DataCenter");

        assertEquals("web1", node.getName());
        assertEquals("10.0.0.1", node.getAddress());
        assertEquals("DataCenter", node.getContainer());
    }
}
