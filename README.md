Build Instructions
==================

Pre-requisites: Apache Maven

Command to build: 
mvn assembly:assembly

The jar file team21.jar would be created in the target directory.

Usage Instructions
==================

To run the predictor use the following command:

java -jar team21.jar <input_file_arff_path> [options]

Options:
--dumpResult    Dumps the result of the evaluation into a .run file
