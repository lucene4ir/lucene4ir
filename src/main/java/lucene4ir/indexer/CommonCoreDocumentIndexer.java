package lucene4ir.indexer;

import lucene4ir.Lucene4IRConstants;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.jsoup.safety.Whitelist;

import java.io.BufferedReader;
import java.util.ArrayList;

/**
 * Created by leif on 03/09/2016.
 * Edited by Abdulaziz AlQattan on 16/03/2019.
 */
public class CommonCoreDocumentIndexer extends lucene4ir.indexer.DocumentIndexer {

    // Properties
    Whitelist whiteList;
    private org.jsoup.nodes.Document jdoc;
    ArrayList<Field> fields;

    // Sub- Private Functions

    private String getFieldAttribute(String TagName  , String AttributeName )
    {
        /*
        This Function is used to get the attribure value of a specific tag
        in a specific jsoup Document
         */
        String selector =  TagName + "[" + AttributeName + "]";
        return  jdoc.select(selector).attr(AttributeName) ;
    }
    private String getFieldText(String TagName){
           /*
           This function is used to retrieve gathered text from  all elements with a specific tag
           in the input jsoup document
            */
        String fieldText = "";
        org.jsoup.select.Elements dns = jdoc.getElementsByTag(TagName);
        if (dns.size() > 0)
            fieldText = dns.text();

        return fieldText;
    }

    private boolean isNumeric(String strNum) {

        /*
        This Function is used to check whether an input string is numeric or not
         */
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }

    private String getPubDate()
    {

        /*
        This function is Used to get the Publication Date Supposing that :
        the value of content for and element with (Key = name and value = publication_day_of_month) = Day
        the value of content for and element with (Key = name and value = publication_month) = month
        the value of content for and element with (Key = name and value = publication_year) = year

        if any values of the return date is empty or non-Numeric return empty string

         */
        String result = "" , aDay , aMonth , aYear;

        aDay = jdoc.getElementsByAttributeValue("name", "publication_day_of_month").attr("content");
        if (!isNumeric(aDay))
            return result;
        aMonth = jdoc.getElementsByAttributeValue("name", "publication_month").attr("content");
        if (!isNumeric(aMonth))
            return result;
        aYear = jdoc.getElementsByAttributeValue("name", "publication_year").attr("content");
        if (!isNumeric(aYear))
            return result;

        result = aDay + '/' + aMonth + "/" + aYear;
        return result;
    }

    private String removeSpecialCharacters (String input)
    {
        input = input.replace("%20" , " ");
        return input;
    }

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
        1= Document Number - String Field
        2- Publish Date - String Field
        3- Title - Text Field
        4- Content - Text Field
        5- All - Text Field
 */
        fields = new ArrayList<Field>();
        addFieldToDocument(Lucene4IRConstants.FIELD_DOCNUM,'s');
        addFieldToDocument(Lucene4IRConstants.FIELD_PUBDATE,'s');
        addFieldToDocument(Lucene4IRConstants.FIELD_TITLE,' ');
        addFieldToDocument(Lucene4IRConstants.FIELD_CONTENT,' ');
        addFieldToDocument(Lucene4IRConstants.FIELD_ALL,' ');
    }

    // Constructor Method
    public CommonCoreDocumentIndexer(String indexPath, String tokenFilterFile, boolean positional){
        super(indexPath, tokenFilterFile, positional);
        // Create String Corpus Fields and add them to the Document doc

        initFields();
        initWhiteList();
    }

    private void initWhiteList()
    {
        // Add White List Of The Document
        // The White List Are The Tags to keep in the Document after Reading From Jsoup
        try {
            // Add The popular tags in the white list
            whiteList = Whitelist.relaxed();

            // Add each allowed tag with its allowed attributes
            whiteList.addTags("title");

            // *******************

            whiteList.addTags("meta");
            whiteList.addAttributes("meta","name" , "content");

            // *******************

            whiteList.addTags("doc-id");
            whiteList.addAttributes("doc-id","id-string");

            // *******************

            whiteList.addTags("doc.copyright");
            whiteList.addAttributes("doc.copyright","year" , "holder");

            // *******************

            whiteList.addTags("classifier");
            whiteList.addAttributes("doc.copyright","year" , "holder");

            // *******************

            whiteList.addTags("pubdata");
            whiteList.addAttributes("pubdata","name");

            whiteList.addTags("hl1");

        } catch (Exception e){
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
        }
    }

    public void indexDocumentsFromFile(String filename){

        /*
        This method is used to  :
            1- read an input file (filepath) Line by Line
            2- Gather all of these lines in one Text String
            3- add a Line Separator between Lines in the Resultant Text
            4- Send The Resultant Text To The Method extractFieldsFromXmlAndIndex
                to identify the xml tags and Index them according to the needs
         */
        String line , XMLText = "";
        short lineNumber = 1;
        try {
            BufferedReader br = openDocumentFile(filename);
            try {
                line = br.readLine();
                while (line != null){
                    if (lineNumber > 3 && !line.startsWith("</nitf>"))
                        XMLText += line + System.lineSeparator();
                    line = br.readLine();
                    lineNumber++;
                }

            } finally {
                if (!XMLText.isEmpty())
                    extractFieldsFromXmlAndIndex(XMLText);
                br.close();
            }
        }
        catch (Exception e){
            System.out.println(" caught a " + e.getClass() +  e.getLocalizedMessage() +
                    "\n with message: " + e.getMessage());
            System.exit(0);
        }
    }

       public void extractFieldsFromXmlAndIndex(String xmlString){
            String   docnum,
                     title ,
                     content,
                     source="Unknown",
                     pubdate ="",
                     safeText = org.jsoup.Jsoup.clean(xmlString,whiteList),
                    values[] = new String[5];
           Document doc;
            jdoc = org.jsoup.Jsoup.parse(safeText);

            docnum = getFieldAttribute("doc-id" , "id-string").trim();
            docnum = removeSpecialCharacters(docnum);

            title = getFieldText("title");
            title = removeSpecialCharacters(title);
            System.out.println(title);
            pubdate = getPubDate();
            source = getFieldAttribute( "pubdata" , "name");
            source = removeSpecialCharacters(source);

            content = pubdate;
            content += getFieldAttribute("doc.copyright","year" ) + " ";
            content += getFieldAttribute("doc.copyright","holder") + " ";
            content += getFieldText( "classifier") + " ";
            content += source + " ";
            content  += getFieldText( "hl1") + " ";
            content += getFieldText( "p") + " ";

            String all = title + " " + content + " " + source + " " + pubdate;

          /*  The Fields Sequence
            1= Document Number - String Field
           2- Publish Date - String Field
           3- Title - Text Field
           4- Content - Text Field
           5- All - Text Field
           */
            values[0] = docnum;
           values[1] = pubdate;
           values[2] = title;
           values[3] = content;
           values[4] = all;

            doc = new Document();
            for (short i = 0 ; i < values.length ; i++)
            {
                fields.get(i).setStringValue(values[i]);
                doc.add(fields.get(i));
            }
           System.out.println(String.format("Adding document: %s Title %s" , docnum , title));
           // Add the resultant document to the Indexer
           addDocumentToIndex(doc);
        }


}