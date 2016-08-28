# lucene4ir

Lucene for Information Retrieval Research and Evaluation

## Setup
We are using IntelliJ by JetBrains (https://www.jetbrains.com/idea/download/),
so once you clone the repo you'll need to see up a few things so that you can run and compile
the apps.

Assuming you are using IntelliJ, go to File, Project Structure, then the Project tab.

- Set the Project SDK, we are using Java 1.8.
- Set the Project Language Level, we are using 8.
- Set the Project Compilier output directory, we have pointed it to ~/lucene4ir/out/

Next, go to the Modules tab and mark the following directories as follows.
- Mark the ~/lucene4ir/src directory as Sources
- Mark the ~/lucene4ir/test directory as Tests

Also, +Add Content Root, to be ~/lucene4ir/

Then, go to the SDKs tab, and add in the jars in ~/lucene4ir/jars. We are using Lucene 6.2 for this demo code.

