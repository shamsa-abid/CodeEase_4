package shamsa.cbrs._1_transactiontable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import Utilities.Constants;
import Utilities.Database;

public class ProjectStrategy implements TableCreationStrategy {
	private static Database db;
	@Override
	public void create() throws ClassNotFoundException, SQLException {
		
		db = Database.getInstance();
		ResultSet rs = getFilesContainingMethodClones();
		
		
        String currentFileName = "";
        String transaction ="";       
        ArrayList<String> previousCloneIDs = new ArrayList<String>();
        String currentCloneID = "";    
        String previousProjectName = "";
        String currentProjectName = "";
        
        while ( rs.next() ) 
        {
        	currentFileName = rs.getString("file");
        	currentCloneID = rs.getString("cloneID");
        	currentProjectName = getProjectName(currentFileName);
        	
        	
        	if (currentProjectName.equalsIgnoreCase(previousProjectName))//same project
        	{
        		//The following if condition avoids the issue of clones in the same project, only one cloneID per project is reported
        		if (!previousCloneIDs.contains(currentCloneID)) 
        		{
            		transaction = transaction.concat(" " + currentCloneID);
        		}           		           		
        		
        	}
        		
        	else//new project
        	{
        		if(!transaction.contentEquals(""))
        		{
        		System.out.println(transaction);
        		}
                //transaction= currentProjectName + " ";                    	
                transaction= "";      
                transaction = transaction.concat(currentCloneID);     
                
                previousCloneIDs.clear();//reset the previous clone IDs 
           
        	}
        	
        	previousCloneIDs.add(currentCloneID);            
            previousProjectName = currentProjectName;
        }
        db.closeConnection();
	}
	
	
private String getProjectName(String currentFileName) {
	if(Constants.PROJECTS_TYPE.contentEquals("Sourcerer"))
	{
	int length = Constants.PROJECT_DIRECTORY.length();
	String fileNameWithoutRootDirectory = currentFileName.substring(length+1, currentFileName.length());
	String fileNameTrimmedToBatchName = fileNameWithoutRootDirectory.substring(fileNameWithoutRootDirectory.indexOf("repo")+5, fileNameWithoutRootDirectory.length());
	String projectBatch = fileNameTrimmedToBatchName.substring(0, fileNameTrimmedToBatchName.indexOf("\\"));
	String fileNameTrimmedToProjectName = fileNameTrimmedToBatchName.substring(projectBatch.length()+1, fileNameTrimmedToBatchName.length());
	String projectName = fileNameTrimmedToProjectName.substring(0, fileNameTrimmedToProjectName.indexOf("\\"));
	return projectBatch + projectName;
	}
	else
	{
		int length = Constants.PROJECT_DIRECTORY.length();
		String fileNameWithoutRootDirectory = currentFileName.substring(length+1, currentFileName.length());
		String projectName = fileNameWithoutRootDirectory.substring(0, fileNameWithoutRootDirectory.indexOf("\\"));
		return projectName;
	}
	
	}


private ResultSet getFilesContainingMethodClones() throws ClassNotFoundException, SQLException {
		
		Connection conn = db.getConnection();
		Statement stmt = conn.createStatement(); 
		 
		String query = "select file,cloneID"+" "+
		"from"+
		"(" +
		"select clones.cloneID, clones.methodID ,methods.file " +
		"from clones " +
		"left join methods " +
		"on clones.methodID = methods.id " +
		"where methods.file is not NULL " +
		"order by methods.file " +
		") as t " +
		"order by file;";
				
		return stmt.executeQuery(query);
}

}
