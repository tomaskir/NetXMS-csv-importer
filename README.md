# NetXMS .csv importer

## What is this?
This software allows mass node import into NetXMS from a .csv file.<br>
It was created to ease the transition from other NMSs into NetXMS.

It is built against the official NetXMS Java client, version 6.1.1.

## Requirements
Java 17 or newer is required to run the importer.

## How to use the importer.
You can download the .jar from the [Releases](https://github.com/tomaskir/NetXMS-csv-importer/releases) page.

2 configuration files are required.
These should be placed in the working directory of the application (same directory as the .jar file).
More on the configuration files in the next section.

After downloading the .jar and creating the configuration files, you can run the importer using:
> java -jar netxms-csv-importer-6.1.1.jar

## Configuration files

##### config.properties
This file sets the basic configuration of the importer.<br>
You can copy the example file from the 'examples' directory and modify it to fit your environment.

Available settings:

| Key | Description |
| --- | --- |
| csv.file.name | Name of the .csv file to import, resolved in the working directory. |
| csv.separator.regex | Regular expression used to split each .csv line into fields. |
| netxms.address | Hostname or IP address of the NetXMS server. |
| netxms.port | Client port of the NetXMS server (default 4701). |
| netxms.username | NetXMS user to log in as. |
| netxms.password | Password for the NetXMS user. |
| import.create.containers | If true, a missing container is created under "Infrastructure Services" and used as the node's parent. If false, nodes with a missing container are skipped. |

##### nodes.csv
This file contains the actual nodes to import into NetXMS.<br>
The name of this file can be configured in 'config.properties'.<br>
An example of the properly formatted .csv file is present in the 'examples' directory.

The first line is treated as a header and is skipped. Every other line must contain exactly 3 fields:

> Node name, Address, Container to bind to

The address can be an IPv4 address, an IPv6 address, or a resolvable hostname.
A node is skipped if a node with the same name or address already exists on the server.

## Building from source
Requires JDK 17 or newer and Maven.
> mvn clean package

This produces `target/netxms-csv-importer-6.1.1-jar-with-dependencies.jar`.

Run the unit tests with:
> mvn test
