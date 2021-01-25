# NetXMS .csv importer

## What is this?
This software allows mass node import into NetXMS from a .csv file.<br>
It was created to ease the transition from other NMSs into NetXMS.

## How to use the importer.
You can download the .jar from the [Releases](https://github.com/tomaskir/NetXMS-csv-importer/releases) page.

2 configuration files are required.
These should be places in the working directory of the application (same directory as the .jar file).
More on the configuration files in the next section.

After downloading the .jar and creating the configuration files, you can run the importer using:
> java -jar netxms-csv-importer.1.1.4.jar

## Configuration files

##### configuration.properties
This file sets the basic configuration of the importer.<br>
You can copy the example file from the 'examples' directory and modify to it to fit your environment.
##### nodes.csv
This file contains the actual nodes to import into NetXMS.<br>
The name of this file can be configured in the 'configuration.properties' file.<br>
Example of the properly formatted .csv file is present in the 'examples' directory.
