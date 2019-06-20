package lucene4ir.indexer;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import lucene4ir.Lucene4IRConstants;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.io.BufferedReader;
import java.util.ArrayList;

/**
 * Created by leif on 03/09/2016.
 * Edited by Abdulaziz on 19/03/2019.
 */

public class JsonLineDocumentIndexer extends lucene4ir.indexer.DocumentIndexer {



    // Properties
    ArrayList<Field> fields;
    private void addFieldToDocument(String fldName , char fldType)
    {
         /*
        This Function is Used to Create a Field based on the input FieldName and FieldType
            if The Field Type = "s" then String Field
            Else ( TextField or TermVectorTextField According to the current indexPositions
            Then add the resultant field to the Document
         */
        Field aField;
        if (fldType == 's')
            aField = new StringField(fldName, "", Field.Store.YES);
        else if (indexPositions)
            aField = new TermVectorEnabledTextField(fldName, "", Field.Store.YES);
        else
            aField = new TextField(fldName, "", Field.Store.YES);
        fields.add(aField);
    }
    private void initFields()
    {
        /*
        Initialize Fields Collection in the Following Sequence :
        1- Document Number
        2- Title
        3- Paragraph
        4- ALL
        Then Add them to the fields Collection
 */
        fields = new ArrayList<Field>();
        addFieldToDocument(Lucene4IRConstants.FIELD_DOCNUM,'s');
        addFieldToDocument(Lucene4IRConstants.FIELD_TITLE,' ');
        addFieldToDocument("Paragraph",' ');
        addFieldToDocument(Lucene4IRConstants.FIELD_ALL,' ');
    }

    // Constructor Method (Initialization)
    public JsonLineDocumentIndexer(String indexPath, String tokenFilterFile, boolean positional){

        super(indexPath, tokenFilterFile, positional);
        {
             // Constructor Method to initialize the Fields Collection
         initFields();
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
        1- Document Number
        2- Title
        3- Paragraph
        4- ALL
        */
        // Local Variables
        Document doc;
        JsonParser jParser;
        JsonElement jElement;
        String  fldName , // Current Field Name
                fldValue, // Current Field Value
                allValue; // Gathering All Value

        short i ; // iterator
        // Parse The input Line
        jParser = new JsonParser();
        jElement = jParser.parse(line);


        // Check If the Line is a Valid Json Object
        if (jElement.isJsonObject()) {
          doc = new Document();
            i=0;
            allValue = "";
            for (Field fld:fields)
            {
                // get The Value From The Current Key According to its name
                fldName = fld.name();
                if (i < fields.size() - 1)
                {
                    // Getting The Value From Json Document and Making All Value
                    fldValue = jElement.getAsJsonObject().get(fldName).getAsString();
                  //  fldValue = corpusDocument.removeSpecialCharacters(fldValue);
                    allValue += " " + fldValue;
                }
                else
                    fldValue = allValue.trim();
                // Set The Value in CorpusFields Collection
                fld.setStringValue(fldValue);
                doc.add(fld);
              //  outputText += fldName + " : " + fldValue + separator;
            } // End For
            addDocumentToIndex(doc);
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
