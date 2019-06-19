package lucene4ir.AbdulazizClasses;


import lucene4ir.indexer.TermVectorEnabledTextField;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.util.ArrayList;


/*
a Class to Gather Information about a Specific Corpus Field
 */
public  class CorpusDocument {

    // Properties of CorpusDocument
    public ArrayList<Field> corpusFields;
    public Document doc;

        // Constructor Method

    public CorpusDocument()
    {
        doc = new Document();

        corpusFields = new ArrayList<Field>();
    }

    public void addFieldToDocument(boolean indexPositions , String fldName , String fldType)
    {
         /*
        This Function is Used to Create a Field based on the input FieldName and FieldType
            if The Field Type = "s" then String Field
            Else ( TextField or TermVectorTextField According to the input indexPositions
            Then add the resultant field to the Document
         */

        Field aField;
        if (fldType.trim().toLowerCase() == "s")
            aField = new StringField(fldName, "", Field.Store.YES);
        else if (indexPositions)
            aField = new TermVectorEnabledTextField(fldName, "", Field.Store.YES);
        else
            aField = new TextField(fldName, "", Field.Store.YES);

        corpusFields.add(aField);
        doc.add(aField);
    }

    public String removeSpecialCharacters (String input)
    {
        input = input.replace("%20" , " ");
        return input;
    }

    public Field setFieldValue (int fldNumber , String value )
    {
        corpusFields.get(fldNumber).setStringValue(value);
        return corpusFields.get(fldNumber);
    }
} // Class End
