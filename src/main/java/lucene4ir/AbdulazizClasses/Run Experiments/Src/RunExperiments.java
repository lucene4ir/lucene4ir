

import lucene4ir.RetrievalApp;
import java.io.*;
import java.util.ArrayList;



public class RunExperiments {
     private String model,

            // Output Directories
            retrievalScriptsDirectory ,
            bashOutputDirectory ,
            retrievalResultDirectory,
            trecOutputDirectory ,
            retrievalParametersDirectory,
            // End Output Directories

            // Input Directories

            inputDirPrefix = "Input/",
            inputExperimentDirectory = inputDirPrefix + "Experiments",
            inputIndexDirectory = inputDirPrefix + "Index",
            inputQueriesDirectory = inputDirPrefix + "Queries",
            inputQrelsDirectory = inputDirPrefix + "Qrels",
            inputTokenFiltersDirectory = inputDirPrefix + "TokenFilters",

          //  retrievalParameterFile = "retrieval_params_Iterator.xml",
            experimentName,
           /*
           1- Trec_Eval Line
            C:/Users/kkb19103/Desktop/trec_eval.8.1/trec_eval
           2- Qrels Part
            C:/Users/kkb19103/Desktop/trec_eval.8.1/sample/307-690.qrels
           3- Res Part
            C:/Users/kkb19103/Desktop/trec_eval.8.1/sample/out/PorterStem-BM25-1.0-1.0.res
           4- outputPart
                >> C:/Users/kkb19103/Desktop/trec_eval.8.1/sample/trec_eval/trecPorterStem-BM25-1.0-1.0.txt
            */
           // Trec_Eval Command Including its path
            qrel,
            experimentDynamicName;

            // Value Lists for b , k , c , mu values
            ArrayList<Float>  vList1 , vList2;
            // List Of Trec_Eval Files to get Map Values
            ArrayList<String> trec_evalFiles , retrievalScripts , bashScripts;
            XMLTextParser currentXMLExperiment; // Current Experiment XML File to modify and save for RetrievalApp

    public static void main (String args[])
    {
        RunExperiments re = new RunExperiments();
    }

    private String getRootFolder()
    {
        // Get the root folder of the current project
        return System.getProperty("user.dir").replaceAll("\\\\","/");
    }

    private ArrayList<Float> processValues (String inVal)
    {
       /*
       This Function Takes a Given input String In The Following Format
       Start:Step:End
       and Retrieve Array of Float Numbers Starting From start value to the End Value
        */
        ArrayList<Float> result = new ArrayList<Float>();
        float start , end , step;
        String[] parts ;
        parts = inVal.split(":");
        start = Float.parseFloat(parts[0]);
        step = Float.parseFloat(parts[1]);
        end = Float.parseFloat(parts[2]);

        for (float i = start ; i <= end ; i+=step)
            result.add(i);

        return result;
    }

    private boolean equalString (String str1 , String str2)
    {
        return str1.toLowerCase().compareTo(str2.toLowerCase()) == 0;
    }

    private void initializeExperimentOutput()
    {
        String outputDirPrefix = "output/",
                cdCommand;
        // Output Directories
        retrievalScriptsDirectory = outputDirPrefix + "RetrievalScripts/";
        bashOutputDirectory = outputDirPrefix + "BashScripts/";
        retrievalResultDirectory = outputDirPrefix + "Results/";
        trecOutputDirectory = outputDirPrefix + "Performance/";
        retrievalParametersDirectory = outputDirPrefix + "Params/";
        // Input Directories

        // Initialize output Scripts
        trec_evalFiles = new ArrayList<String>();
        retrievalScripts = new ArrayList<String>();
        cdCommand = String.format("cd '%s'\n", getRootFolder());
        retrievalScripts.add(cdCommand);
        bashScripts = new ArrayList<String>();
        bashScripts.add(cdCommand);

        // Create Separate Experiment Directory
        retrievalResultDirectory = createExperimentOutputFolder(retrievalResultDirectory);
        trecOutputDirectory = createExperimentOutputFolder(trecOutputDirectory);
        retrievalParametersDirectory = createExperimentOutputFolder(retrievalParametersDirectory);

    }

    private void ConvertExperimentToRetrievalParamsFile()
    {
          //  This Function is used to Convert The Input Experiment XML File to Retrieval Parameters XML File

        // Set Source Folders for Tags

        currentXMLExperiment.setTagFolder ("indexName" , inputIndexDirectory);
        currentXMLExperiment.setTagFolder( "queryFile" , inputQueriesDirectory);
        currentXMLExperiment.setTagFolder( "qrelFile" , inputQrelsDirectory);
        currentXMLExperiment.setTagFolder( "tokenFilterFile" , inputTokenFiltersDirectory);
        if (equalString(model,"bm25"))
        {
            currentXMLExperiment.removeTag("cRange");
            currentXMLExperiment.removeTag("muRange");
            currentXMLExperiment.renameTag ("bRange","b");
            currentXMLExperiment.renameTag ("kRange","k");
        } // End if
        else if (equalString(model,"LMD"))
        {
            currentXMLExperiment.removeTag("cRange");
            currentXMLExperiment.removeTag("bRange");
            currentXMLExperiment.removeTag("kRange");
            currentXMLExperiment.renameTag("muRange","mu");
        } // End Else
        else if (equalString(model,"PL2"))
        {
            currentXMLExperiment.removeTag("muRange");
            currentXMLExperiment.removeTag("bRange");
            currentXMLExperiment.removeTag("kRange");
            currentXMLExperiment.renameTag("cRange","c");
        } // End else
    }

    private void processResults()
    {
        /*
        This Function is Used to write XML Text in the Paramneter File
        and Send it to The Retrieval App
         */
        String retrievalParameterFile ,
                retrievalResultsFile,
                retrievalScript;

            // Write Into Results Folder
            retrievalResultsFile = retrievalResultDirectory + experimentDynamicName + ".res";
            currentXMLExperiment.setTagValue("resultFile", retrievalResultsFile);

            // Write Into Params Folder
            retrievalParameterFile = retrievalParametersDirectory + experimentDynamicName + ".params";
            writeFile(retrievalParameterFile,currentXMLExperiment.parsedXML);
            RetrievalApp re = new RetrievalApp(retrievalParameterFile);
            re.processQueryFile();

            // RetrievalApp Script Java … retrievalApp params/experiment_one_0.1.params
        retrievalScript = String.format("java retrievalApp %s \n", retrievalParameterFile);
        retrievalScripts.add(retrievalScript);

    }

     private void addBashCommand()
     {
          /*
          This Function is used to get a Bash Command Line to put later in bashFile
          in The Following Format

                   Trec_Eval Line
                   C:/Users/kkb19103/Desktop/trec_eval.8.1/trec_eval
                   Qrels Part
                    C:/Users/kkb19103/Desktop/trec_eval.8.1/sample/307-690.qrels
                    Res Part
                    C:/Users/kkb19103/Desktop/trec_eval.8.1/sample/out/PorterStem-BM25-1.0-1.0.res
                    outputPart
                    >> C:/Users/kkb19103/Desktop/trec_eval.8.1/sample/trec_eval/trecPorterStem-BM25-1.0-1.0.txt*/
        String bashCommand ,
               trecOutputFile, // Trec Output
                // "C:/Users/kkb19103/Desktop/trec_eval.8.1/trec_eval";
                trecEvalCommand = getRootFolder() + "/Src/Dependancies/trec_eval.8.1/trec_eval";

         trecOutputFile = String.format("%sTrec-%s.perf",trecOutputDirectory , experimentDynamicName);
         trec_evalFiles.add(trecOutputFile);
         bashCommand = String.format("'%s' -q './%s' './%s' > './%s' \n",
                 trecEvalCommand,
                 qrel,
                 retrievalResultDirectory + experimentDynamicName + ".res"  ,
                 trecOutputFile);
         bashScripts.add(bashCommand);
     }

     private void processRetrievalScriptFile()
     {
         String retrievalScriptText = "",
                 retrievalScriptFileName;

         retrievalScriptFileName = retrievalScriptsDirectory + experimentName + ".sh";

         for (String retrievalCommand:retrievalScripts)
             retrievalScriptText+=retrievalCommand;

         writeFile(retrievalScriptFileName,retrievalScriptText);

     }
    private void processBashScriptFile()
    {
        /*
       This Function is Used to
       1- use the array of Bash Commands got from Retrieval Experiments
       2- Write them in a separate bash File
       3- run the resultant bash file
         */

        String bashCommandText = "";
        String bashFilePath = bashOutputDirectory + experimentName  + ".sh";


       for (String bshCommand:bashScripts)
           bashCommandText += bshCommand;
       bashCommandText = bashCommandText.replaceAll("'" , "\"") ;
       writeFile(bashFilePath,bashCommandText);
        try {
            // "C:/cygwin64/bin/bash -c " + bashFilePath
         //   bashCommandText = "C:/cygwin64/bin/bash -c " + bashFilePath;
            bashCommandText = String.format("sh \"%s/%s\"", getRootFolder() , bashFilePath);
            Runtime.getRuntime().exec(bashCommandText).waitFor();
        } // End Try
        catch (Exception eRunTime)
        {
            System.out.println("Error in running BashFile " + bashFilePath );
            System.exit(0);
        } // End Catch

    } // End Function

    private void processDynamicOutput()
    {
        /*
        This function is used to process Dynamic output the is changing based on
        tuning values (b,k,mu,c)
         */
        processResults();
       addBashCommand();
    } // End Function

    private String createExperimentOutputFolder(String outputDirectory)
    {
        /*
            This Function is Used to Create Separate Folder For Current Experiment Output
            in Performance Folder and Results Folder
            1- It takes the outputDirectory (Ex. Results or Performance)
            2- Attach it to the root folder for bringing full path
            3- Attach suggested Experiment Folder Name
            4- Check if The Directory Exist >>> if not create it
         */

        String rootPath = getRootFolder(),
                folderPath;
        File exFolder; // Current Experiment Folder

        // Put into Results Folder
        folderPath = String.format("%s/%s%s", rootPath , outputDirectory ,experimentName);
        exFolder = new File(folderPath);
        // Check if Directory Exist
        if (!exFolder.exists())
            exFolder.mkdir();

        return outputDirectory + experimentName + "/";
    }

    private void writeFile (String fileName , String FileText)
    {
        try {
            PrintWriter pr = new PrintWriter(fileName);
            pr.write(FileText);
            pr.close();
        } catch (FileNotFoundException e) {
            System.out.println(String.format("File %s is not found for writing" , fileName) );
            System.exit(0);
        }
    }

   /*
    private String getMAPValue (String fileName)
    {
        String result = "", parts[],
                line;
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileName));
            line = br.readLine();
            while (line != null)
            {
                if (line.startsWith("map"))
                {
                    parts = line.split("\t");
                    result = parts[2].trim();
                    break;
                }
                line = br.readLine();
            }
        }
       catch (Exception e)
       {
           System.out.println("Error in Writing MAP File " + fileName);
           System.exit(0);
       }
        return result;
    }

   private String getValuesLine (ArrayList<Float> inValues)
    {
        String result = "[";
        int i;
        for (i = 0 ; i < inValues.size() ; i++)
            result += inValues.get(i) + " , ";

         i = result.lastIndexOf(",");
        result = result.substring(0,i-1) + "]";
        return result;
    }

    private void processMAPValues()
    {
        String mapValue , vList1Line , vList2Line , MAPValuesLine , outputText;
        ArrayList<Float> mapValues = new ArrayList<Float>();

        // Access Trec_Eval Directory and iterate Each File
            for (String trecFile:trec_evalFiles)
            {
                // Get MAP Values from each File and Gether Them in an array
                mapValue = getMAPValue(trecFile);
                if (!mapValue.isEmpty())
                    mapValues.add(Float.valueOf(mapValue));
            }
        outputText = "Experiment : " + experimentName + System.lineSeparator();
       if (mapValues.size() == vList1.size())
       {
            if (equalString(model , "bm25"))
            {
                outputText += String.format(" b  : %s \n",  getValuesLine(vList1));
                outputText += String.format(" k  : %s \n",  getValuesLine(vList2));
            }
            else if (equalString(model,"PL2"))
                outputText += String.format(" c  : %s \n",  getValuesLine(vList1));
            else if (equalString(model,"LMD"))
                outputText += String.format(" mu : %s \n",  getValuesLine(vList1));

            outputText += String.format("map : %s \n",  getValuesLine(mapValues));

            writeFile(MapValuesOutputDirectory + experimentName + ".txt" , outputText);
        }
       else
           System.out.println("Error : MAP Values Does Not Match Iteration values (b,c or mu )");
    }*/

    public void processExperiment(String experimaneFile)
    {
        /*
        This Function is Used to run Specific Experiment :
         */

        String separator = "-", // Special separator for the name of output file
                tempModel = "";

        currentXMLExperiment = new XMLTextParser(experimaneFile);
        // After Gathering XML Text Set The input Values in it
        if (!currentXMLExperiment.parsedXML.isEmpty())
        {
            experimaneFile = experimaneFile.substring(experimaneFile.lastIndexOf("\\") + 1 ,experimaneFile.lastIndexOf("."));
            experimentName = experimaneFile;
            initializeExperimentOutput();
            // Get ModelType
            model = currentXMLExperiment.getTagValue("model");
            qrel = currentXMLExperiment.getTagValue("qrelFile");
            if (!qrel.contains(inputQrelsDirectory))
                qrel = inputQrelsDirectory + "/" +  qrel;

            // Extract Value Range Based On Model Type
           // BM25
            if (equalString(model , "bm25"))
            {
                tempModel =  currentXMLExperiment.getTagValue("bRange");
                vList1 = processValues(tempModel);
                tempModel = currentXMLExperiment.getTagValue("kRange");
                vList2 = processValues(tempModel);
                ConvertExperimentToRetrievalParamsFile();
                for (int b = 0 , k = 0 ; b < vList1.size() && k < vList2.size() ; b++ , k++)
                {
                    // Assign Dynamic Values
                    experimentDynamicName = experimentName + separator + vList1.get(b)+ separator+ vList2.get(k);
                    currentXMLExperiment.setTagValue("b", String.valueOf(vList1.get(b)));
                    currentXMLExperiment.setTagValue("k", String.valueOf(vList2.get(k)));
                    processDynamicOutput();
                } // End For
            } // End if
            // LMD
            else
            {
                if (equalString(model , "LMD"))
                    tempModel = "mu";
                else if (equalString(model , "PL2"))
                    tempModel = "c";

                vList1 = processValues(currentXMLExperiment.getTagValue(tempModel + "Range"));
                ConvertExperimentToRetrievalParamsFile();
                for (int i = 0 ; i<vList1.size();i++)
                {
                    // Assign Exoeriment Dynamic Name
                    experimentDynamicName = experimentName + separator + vList1.get(i);
                    // Assign Dynamic Values
                    currentXMLExperiment.setTagValue(tempModel, String.valueOf(vList1.get(i)));
                    processDynamicOutput();
                } // End For
            }
               processBashScriptFile();
                processRetrievalScriptFile();
            } // End if (!ExperimentXMLText.isEmpty())
    }

    public RunExperiments()
    {
        /*
        Constructor Method for running Specific Experiemtn in Experiments Folder
        if ExperimentName is Empty Run All Experiments available in Experiments Folder
         */

        // Local Variables
        CrossDirectoryClass cd;
        ArrayList<String> ExperimentFiles;

        // Cross Experiments Folder and get All Files in it
        cd = new CrossDirectoryClass();
        ExperimentFiles = cd.crossDirectory(inputExperimentDirectory,false);
        if (ExperimentFiles.size() > 0)
            for (String exFile:ExperimentFiles)
                processExperiment(exFile);
        else
            System.out.println("No Experiment Found");

    } // End Constructor Function

} // End Class