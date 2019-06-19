package main.java.lucene4ir.indexer;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lucene4ir.AbdulazizClasses.CorpusDocument;
import lucene4ir.Lucene4IRConstants;
import org.apache.lucene.document.Field;

import java.io.BufferedReader;

/**
 * Created by leif on 03/09/2016.
 * Edited by Abdulaziz on 19/03/2019.
 */

public class JsonLineDocumentIndexer extends lucene4ir.indexer.DocumentIndexer {



    // Properties

    private CorpusDocument corpusDocument; // Corpus Document with Fields Collection
  //  private String outputText = "";  // Singe output Line

    // Constructor Method (Initialization)
    public JsonLineDocumentIndexer(String indexPath, String tokenFilterFile, boolean positional){

        super(indexPath, tokenFilterFile, positional);
        {
             /*
             This Function is Used to do the following:
                1- Create Corpus Document
                2- add the fields to the document according to the Requirements

            The headers of the Fields in Corpus Line Document That identify The fields and their sequence
            This is The order Of Fields in a Single Json Line in the File
            1- Collection
            2- Document ID
            3- Title
            4- Paragraph ID
            5- Paragraph
    */
             corpusDocument = new CorpusDocument();
            // Collection
         //   corpusDocument.addFieldToDocument(super.indexPositions , "Collection" ,"");
            // Document Number
            corpusDocument.addFieldToDocument(super.indexPositions , Lucene4IRConstants.FIELD_DOCNUM,"s");
            // Title
            corpusDocument.addFieldToDocument(super.indexPositions , Lucene4IRConstants.FIELD_TITLE ,"");
            // Paragraph ID
         //   corpusDocument.addFieldToDocument(super.indexPositions ,  "Paragraph ID" ,"s");
            // Paragraph
            corpusDocument.addFieldToDocument(super.indexPositions , "Paragraph" ,"");
            // All
            corpusDocument.addFieldToDocument(super.indexPositions , Lucene4IRConstants.FIELD_ALL ,"");

        }
    }

   /* private void printOuputToFile()
    {
        String outFileName = "JsonLineResults.txt";
        try{
            System.out.println(outputText);
            PrintWriter pr = new PrintWriter(outFileName);
            pr.write(outputText);
            pr.close();
        }
        catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(0);
        }
    }*/

   public void parseLine(String line)
    {
         /*
        This Function is used to  :
        1- Parse a Single Json Line From Car Corpus Document
        2- Extract the required properties from the Line
        3- Add The Properties to Corpus Fields Collection
        4- Add The corpusFields Collection in the Corpus Document inside the Fields Collection
        5- Index The Resultant Document

        It is very important to mention the order of Fields in the Corpus Document Line
        1- Collection
        2- Document ID
        3- Titles
        4- Paragraph ID
        5- Paragraph
        */
        // Local Variables
        JsonParser jParser;
        JsonElement jElement;
        String  fldName , // Current Field Name
                fldValue, // Current Field Value
                allValue, // Gathering All Value
             //   separator = System.lineSeparator(), // New Line Separator
                fldNames[] = {"Collection","DocID","Title","ParagraphID","Paragraph" , "All"}; // The Field Names as Written in The JsonDocument
        short i ; // iterator
        // Parse The input Line
        jParser = new JsonParser();
        jElement = jParser.parse(line);


        // Check If the Line is a Valid Json Object
        if (jElement.isJsonObject()) {
            corpusDocument.doc.clear();
            i=0;
            allValue = "";
            for (Field iField:corpusDocument.corpusFields)
            {
                // get The Value From The Current Key According to its name
                fldName = fldNames[i++];
                if (i < fldNames.length)
                {
                    // Getting The Value From Json Document and Making All Value
                    fldValue = jElement.getAsJsonObject().get(fldName).getAsString();
                  //  fldValue = corpusDocument.removeSpecialCharacters(fldValue);
                    allValue += " " + fldValue;
                }
                else
                    fldValue = allValue.trim();

                    // Set The Value in CorpusFields Collection
                iField.setStringValue(fldValue);
                corpusDocument.doc.add(iField);
              //  outputText += fldName + " : " + fldValue + separator;
            } // End For

            addDocumentToIndex(corpusDocument.doc);
        } // End if
    }
        public void indexDocumentsFromFile (String fileName){
            // Local Variables for a Single Line and Line Separator
            String line,
                    separator = System.lineSeparator();
            int lineNumber = 0;
            // Read The Input File into a Buffer
            try {
                BufferedReader br = openDocumentFile(fileName);
                line = br.readLine();

                // Iterate Through Lines and Parse Lines each by each
                while (line != null) {
                 /*   outputText += separator +  "Line Number : " + ++lineNumber + separator +
                            "-----------------------------------------" + separator ;*/
                    parseLine(line); // Send The Current Line To The Parse Line Function
                    line = br.readLine();
                } // End While
               /* if (!outputText.isEmpty())
                    printOuputToFile();*/
            }  // End Try
            catch (Exception e) {
                e.printStackTrace();
                System.exit(1);
            } // End CATCH
        } // End Function
}
