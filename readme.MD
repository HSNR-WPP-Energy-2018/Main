# Energy efficiency analysis

When trying to decide whether or not installing a solar array is viable or not the used energy for an household is a main factor.
This applications supports making the decision by filling missing data point from an recorded list of power usage.
Multiple different approaches (algorithms) are implemented as components of this application.

## User workflow

The usage list is provided with a CSV file that contains timestamps and associated usages.
Additionally configuration parameters have to provided for the desired algorithm.
The output file has the same structure as the input file but contains a more complete usage list.

## Application structure

Gradle is used as a tool for managing dependencies and simplifying the build process.
The application consists of multiple packages that serve different purposes. Their usage is explained in this following section.

### Base

The base package contains general classes that are used throughout the application as well as helper classes

- [Algorithm](./src/main/java/de/hsnr/wpp2018/base/Algorithm.java): Basis for all algorithms that defines a common interface that is used to access all algorithms the same way.
- [Helper](./src/main/java/de/hsnr/wpp2018/base/Helper.java): The helper class contains support methods as regards to contents.
- [ParserHelper](./src/main/java/de/hsnr/wpp2018/base/ParserHelper.java): Distinct to the main helper class this class contains helper methods for parsing the input. It is used by the router and during the database construction.
- The remaining classes are used as data models throughout the application.

### IO

- [Importer](./src/main/java/de/hsnr/wpp2018/io/Importer.java): CSV importer for reading the input files.
- [Exporter](./src/main/java/de/hsnr/wpp2018/io/Exporter.java): Simple CSV exporter for exporting the result data into an export file.
- [Parser](./src/main/java/de/hsnr/wpp2018/io/database/Parser.java): Database file parser used to read the persisted database back into memory.
- [Writer](./src/main/java/de/hsnr/wpp2018/io/database/Writer.java): Database writer used to write the database contents info files (one for each entry). A simple custom text based format is used.

### Algorithms

This package contains all algorithms implemented by the application. Their functionality is explained in more detail inside the project paper.
All algorithms are subclasses of the [Algorithm](./src/main/java/de/hsnr/wpp2018/base/Algorithm.java) interface that defines a common usage for all algorithms.

The input parameters required for every algorithm are managed by distinct configuration classes. These are subclasses of the [basic configuration class](./src/main/java/de/hsnr/wpp2018/base/Algorithm.java) defined inside the algorithm interface.

### Optimizations

This package contains the implementations for all heuristics. Their functionality is explained in more detail inside the project paper.

### Database

This package contains all classes related to the database approach. [Database](./src/main/java/de/hsnr/wpp2018/database/Database.java) is the main class for this package. Other classes define the structure for elements inside the database.
The functionality is described in more detail inside the project paper.

### Evaluation

The evaluation package is used to determine the result quality after an algorithm and/or heuristic interpolated the data.
For distinct testing purposes functionality is implemented to generate testing data from a full usage list by removing a user defined share of data points.

- [TestDataGenerator](./src/main/java/de/hsnr/wpp2018/evaluation/TestDataGenerator.java): Generating test data by removing a defined percentage of weeks, days, hours and/or single data points.
- [Rating](./src/main/java/de/hsnr/wpp2018/evaluation/Rating.java): Rating the interpolation result based on the comparison between a provided truth dataset and the result.
- [Analyser](./src/main/java/de/hsnr/wpp2018/evaluation/Analyser.java): The analyser is used to evaluate a provided dataset and give recommendations for an algorithm that should produce the best result.
- The remaining classes are used as data models by the other classes.

## Dependencies

The dependencies are managed by the build tool Gradle.
For production use only the Apache commons math library is required. For testing the application JUnit is used.

## Testing

For all algorithms and the IO tests are written in different unit test files inside the [testing package](./src/test/java/de/hsnr/wpp2018).
In addition to basic test classes that evaluate the functionality and validity of the implementation more complex tests are implemented for evaluating the performance of algorithms and heuristics for hugely different input dataset structure.