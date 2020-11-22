# LogFileAnalyzer

## Problem

Our custom-build server logs different events to a file named logfile.txt. Every event has 2 entries in
the file - one entry when the event was started and another when the event was finished. The entries
in the file have no specific order (a finish event could occur before a start event for a given id)

Every line in the file is a JSON object containing event data:  


- **id** - the unique event identifier  
- **state** - whether the event was started or finished (can have values "STARTED" or "FINISHED"  
- **timestamp** - the timestamp of the event in milliseconds  

Application Server logs also have the additional attributes:  
- **type** - type of log
- **host** - hostname

**Example:**  

```
{"id":"scsmbstgra", "state":"STARTED", "type":"APPLICATION_LOG",  
"host":"12345", "timestamp":1491377495212}  
{"id":"scsmbstgrb", "state":"STARTED", "timestamp":1491377495213}  
{"id":"scsmbstgrc", "state":"FINISHED", "timestamp":1491377495218}  
{"id":"scsmbstgra", "state":"FINISHED", "type":"APPLICATION_LOG",  
"host":"12345", "timestamp":1491377495217}  
{"id":"scsmbstgrc", "state":"STARTED", "timestamp":1491377495210}  
{"id":"scsmbstgrb", "state":"FINISHED", "timestamp":1491377495216}  
...
```

In the example above, the event scsmbstgrb duration is 1401377495216 - 1491377495213 = 3ms  
The longest event is scsmbstgrc (1491377495218 - 1491377495210 = 8ms)  

The program should:  
- Take the input file path as input argument  
- Flag any long events that take longer than 4ms with a column in the database called "alert"  
- Write the found event details to file-based HSQLDB (http://hsqldb.org/) in the working folder  
- The application should a new table if necessary and enter the following values:  
  - Event id  
  - Event duration  
  - Type and Host if applicable  
  - Alert (true if the event took longer than 4ms, otherwise false)

### Project Hierarchy

The following files and folder are part of the project:

- README.md : current file
- src/main/java :  Source files of the code.
- src/main/resources :  Sample input json file
- src/test/java :  Source files for the JUNIT5 unit tests
- src/test/resources :  input json files used by the unit tests

### Prerequisites

You will need maven version 3.6.3 or higher to be able to compile and run this application

### Installing

Build the application and run the JUnit5 unit tests, from the root folder:

```
mvn clean
mvn compile
mvn package
```

## Executing the application

The execution of the application requires an input file that can be passed as argument of the execution.

```
java -jar target/LogFileAnalyzer-1.0-SNAPSHOT.jar "[<absolute_path_to_input_file>] [<number of threads>]"
```

Examples of valid commands:

```
java -jar target/LogFileAnalyzer-1.0-SNAPSHOT.jar C:\Workspace\LogFileAnalyzer\logfile.txt
java -jar target/LogFileAnalyzer-1.0-SNAPSHOT.jar C:\Workspace\LogFileAnalyzer\logfile.txt 1
```

Note:
	The input file must exist

If the input path does not exist FilePathInvalidException will be raised.

## Expected output

The application creates an HSQLDB table and the following files are expected to be generated at the root folder:
- events.lck : lock file
- events.log 
- events.properties
- events.script : script running at startup to restore the table. You can see the content of the table by checking the "INSERT" rows in this script.
