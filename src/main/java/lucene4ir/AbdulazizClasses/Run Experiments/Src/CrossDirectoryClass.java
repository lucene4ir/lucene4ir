
/*
This Class is used to cross all files in a directory even within inner directories
 */

import java.io.File;
import java.util.ArrayList;

public  class CrossDirectoryClass {
     public int fileCount, // The Number of crossed files
                         folderCount; // The Number of crossed folders
     private ArrayList<String> fileList ; // The Resultant list of file names in a directory


    public CrossDirectoryClass()
    {
        // Constructor Method
        // Initialize Main Variables and Counts
        fileCount = 0;
        folderCount = 0;
        fileList =  new ArrayList<String>() ;
    }

    private  void listDirectory(File aDir , boolean crossInner) {
        /*
        This is a recursive function to traverse all folders and all files in a Given Directory
        and fill the name of files in the input tempList
         */
        for (File aFile : aDir.listFiles())
            if (aFile.isDirectory() )
            {
                if (crossInner)
                {
                    folderCount++;
                    listDirectory(aFile,crossInner);
                } // End if (crossInner)
            } // End (aFile.isDirectory()
            else {
                fileCount++;
                fileList.add( aFile.getAbsolutePath() );
            }
    }

    public ArrayList<String> crossDirectory(String path , boolean crossInner)
    {
        /*
        This function is used to traverse all files in the input Directory path
        Cross inner variable to identify whether to cross inner directories or not
        */

        File directory = new File(path);
        // check  whether the directory is exist of not
        if (directory.exists())
            listDirectory(directory ,crossInner);
        else
            System.out.println("Directory is not Found");
        return fileList;
    }


}
