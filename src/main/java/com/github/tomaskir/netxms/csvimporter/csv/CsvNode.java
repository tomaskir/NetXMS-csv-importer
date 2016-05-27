package com.github.tomaskir.netxms.csvimporter.csv;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public final class CsvNode {
    private final String name;
    private final String address;
    private final String container;
}
