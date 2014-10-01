This document is a very short user guide, for more details, see the implementation.

1. System requirements:
The source code is written in Java 6, it should work on both Windows and Unix/Linux systems that have a JDK compatible with version 1.6 or above

The results reported in our paper was evaluated using Java 8 (64 bit). When evaluated on Java 7, the results are slightly different.

2. Folder structures
Unzip the DDIExtraction.zip to a folder, the following folders and file are created:

|-- DDI_corpora: contains XML files downloaded from the DDIExtraction 2011 and 2013 challenge websites
|-- data: store Java objects
|-- lib: library (jar files)
|-- output: output for false positive and false negatives 
|-- src: Java source code
|-- DDIExtraction.jar
|-- Readme.txt (this file)

3. Run demo

java -cp DDIExtraction.jar relation.Demo <para>

where <para> is one of the following values: DB2011, DB2013, ML2013

For example, to run the DB2011 dataset, enter the following command:

java -cp DDIExtraction.jar relation.Demo DB2011