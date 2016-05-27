package com.github.tomaskir.netxms.csvimporter.csv;

import com.github.tomaskir.netxms.csvimporter.exceptions.InvalidCsvDataException;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CsvParser {
    @Getter
    private final static CsvParser instance = new CsvParser();

    public List<CsvNode> parseCsv(String fileName, String separatorRegex) throws FileNotFoundException, InvalidCsvDataException {
        Scanner fileScanner = new Scanner(new File(Paths.get(fileName).toAbsolutePath().toString()));
        List<CsvNode> nodeList = new ArrayList<>();
        int processedLine = 0;

        // read the data in the .csv file
        while (fileScanner.hasNext()) {
            processedLine++;

            // skip field descriptions in the first line
            if (processedLine == 1) {
                fileScanner.nextLine();
                continue;
            }

            String line = fileScanner.nextLine();
            String[] elements = line.split(separatorRegex);

            if (elements.length != 3)
                throw new InvalidCsvDataException("Invalid data in .csv file - line " + processedLine + "!");

            // cleanup on the data read from the .csv file and validate them
            for (int i = 0; i < elements.length; i++) {
                elements[i] = elements[i].trim();

                if (elements[i].equals(""))
                    throw new InvalidCsvDataException("One of required node properties is empty - line " + processedLine + "!");
            }

            // create node object and add it to the node list
            nodeList.add(new CsvNode(elements[0], elements[1], elements[2]));
        }

        fileScanner.close();
        return nodeList;
    }
}
