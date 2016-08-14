package com.github.tomaskir.netxms.csvimporter;

import com.github.tomaskir.netxms.csvimporter.configuration.ConfigurationLoader;
import com.github.tomaskir.netxms.csvimporter.csv.CsvNode;
import com.github.tomaskir.netxms.csvimporter.csv.CsvParser;
import com.github.tomaskir.netxms.csvimporter.netxms.NetxmsConnector;

import java.util.List;
import java.util.Properties;

public final class Application {
    private static final String PROPERTIES_FILE = "config.properties";
    private static final String CSV_FILE_NAME_PROPERTY = "csv.file.name";
    private static final String CSV_SEPARATOR_REGEX_PROPERTY = "csv.separator.regex";
    private static final String NXMS_ADDRESS_PROPERTY = "netxms.address";
    private static final String NXMS_PORT_PROPERTY = "netxms.port";
    private static final String NXMS_USERNAME_PROPERTY = "netxms.username";
    private static final String NXMS_PASSWORD_PROPERTY = "netxms.password";

    public static void main(String[] args) {
        Properties properties;
        List<CsvNode> nodeList;

        System.out.println("Application starting.");

        try {
            System.out.println("Loading configuration properties file.");
            properties = ConfigurationLoader.getInstance().load(PROPERTIES_FILE);

            System.out.println("Loading nodes from the .csv file.");
            nodeList = CsvParser.getInstance().parseCsv(properties.getProperty(CSV_FILE_NAME_PROPERTY), properties.getProperty(CSV_SEPARATOR_REGEX_PROPERTY));

            System.out.println("Connecting to the NetXMS server.");
            NetxmsConnector.getInstance().connect(properties.getProperty(NXMS_ADDRESS_PROPERTY), properties.getProperty(NXMS_PORT_PROPERTY),
                    properties.getProperty(NXMS_USERNAME_PROPERTY), properties.getProperty(NXMS_PASSWORD_PROPERTY));

            System.out.println("Creating nodes on the NetXMS server.");
            NetxmsConnector.getInstance().addNodes(nodeList);
        } catch (Exception e) {
            System.out.println("Error: " + e.getClass().getSimpleName() + " - " + e.getMessage() + ".");
            return;
        }

        NetxmsConnector.getInstance().disconnect();
        System.out.println("Application finished successfully.");
    }
}
