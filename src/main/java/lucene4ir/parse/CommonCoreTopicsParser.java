package lucene4ir.parse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.PrintWriter;


public class CommonCoreTopicsParser {


    /*
    This Class is Used to Parse CommonCore Topics Document according to the following form :
    <top>
    <num> Number: 307
    <title> New Hydroelectric Projects

    <desc>
    Identify hydroelectric projects proposed or under construction by country and location. Detailed description of nature, extent, purpose, problems, and consequences is desirable.

    <narr>
    Relevant documents would contain as a minimum a clear statement that a hydroelectric project is planned or construction is under way and the location of the project. Renovation of existing facilities would be judged not relevant unless plans call for a significant increase in acre-feet or reservoir or a marked change in the environmental impact of the project. Arguments for and against proposed projects are relevant as long as they are supported by specifics, including as a minimum the name or location of the project. A statement that an individual or organization is for or against such projects in general would not be relevant. Proposals or projects underway to dismantle existing facilities or drain existing reservoirs are not relevant, nor are articles reporting a decision to drop a proposed plan.
    </top>

    Given :
        1- inFile --- Source File Path
        2- output --- Output File Path
        3- QueryType which will be one of the following :
            3.1 TITLEONLY --- Query Terms will be composed using Title Field Only
            3.2 DESCONLY --- Query Terms will be composed using Description Field Only
            3.3 TITLEAndDESC --- Query Terms will be composed using both Title & Description Fields
     */

    // General Enumeration For Query Types
     public enum QryType {
        TITLEONLY , // Query = Title Vlaue Only
        DESCONLY , // Query = Description Vlaue Only
        TITLEAndDESC // Query = Title Vlaue + Description Value
    };

     // Properties to Fill During Constrution of an instance
     QryType classQryType;
     String classInFile , classOutFile;
     PrintWriter pr;

    // Constructor method
    public CommonCoreTopicsParser()
    {
    }

    private void printLine(String outLine)
    {
        // This Function is Used to print a Given out Text in the output File
        System.out.print(outLine);
        pr.write(outLine);
    }

    private void parseXMLFile(String XMLText , QryType inQryType)
    {
        /*
        This Function is used to parse a Full XML CommonCore Document
        and Extract the following information from each topic
        1- Topic Number
        2- Title
        3- Description

         */
        String qryID , // QueryID
                title , // Title
                desc, // Describtion
                qry, // Query
                aLine ; // outputLine

        // Create a jSoup Document and Parse XML in it
        org.jsoup.nodes.Document jdoc = Jsoup.parse(XMLText);
        // get All top tags
        Elements elements = jdoc.getElementsByTag("top");
        // iterate through all top tags
        for (Element iElement : elements)
        {
            // This is the location of the topic number according to the XML Schema
            qryID =  iElement.getElementsByTag("num").get(0).textNodes().get(0).text();
            // remove Number: caption
            qryID = qryID.replace("Number:" , "").trim();
            // Extract the title
             title = iElement.getElementsByTag("title").text();
             // Extract Description
             desc = iElement.getElementsByTag("desc").text();
             // Create the Query according to the input QueryType
             switch (inQryType)
             {
                 case TITLEONLY :
                     qry = title;
                     break;
                 case DESCONLY:
                     qry = desc;
                     break;
                 case TITLEAndDESC:
                     qry = title + " " + desc;
                     break;
                     default:
                         qry = "Not Recognized Query ";
             };
             // Form The output Line
            aLine = qryID + " " + qry + System.lineSeparator();
            printLine(aLine);
        } // End For
     } // End Function

    public void parse(String inFile , String outFile , QryType inQryType)
    {
        /*
        This Function is used to Read the input XML File and gather its lines into a single String variable
        Given the following :
         inFile = The File To Read
        outFile = The Output File Path
        QryType : One Of Three Values
            1- Title Only Query
            2- Description Only Query
            3- Title And Description Query
         */
        String line , XMLText = "";
        try {
            // Initialize The Reader and Writer
            pr = new PrintWriter(outFile);
            BufferedReader br = new BufferedReader((new FileReader(inFile)));
            line = br.readLine();
            while (line != null)
            {
                // Gather All Lines of the Document in One String
                XMLText += line + System.lineSeparator();
                line = br.readLine();
            }
        }
        catch (Exception e)
        {
            System.out.println(" caught a " + e.getClass() +
                    "\n with message: " + e.getMessage());
            System.exit(0);
        }
        // if the gathered text is not empty ... parse it
        if (!XMLText.isEmpty())
            parseXMLFile(XMLText,inQryType);

        // Close and Save The File Writer
        pr.close();

    }

    public static void main(String[] args)
    {
        String queryDir = "data/queries/wapo/",
                inFile = queryDir + "CommonCore-2018-WAPO-Topics.txt",
                outFile = queryDir + "ParsedCommonCore-2018-WAPO-Topics.txt" ;

        CommonCoreTopicsParser cctp = new CommonCoreTopicsParser();
        cctp.parse(inFile,outFile,QryType.TITLEONLY);

    }
}
