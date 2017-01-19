package shamsa.cbrs._1_transactiontable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import Utilities.Database;

public class FileStrategy implements TableCreationStrategy {
	private static Database db;
	@Override
	public void create() throws ClassNotFoundException, SQLException {
		// TODO Auto-generated method stub
		db = Database.getInstance();
		ResultSet rs = getFilesContainingMethodClones();
        String previousFilename = "";
        String currentFileName = "";
        String transaction ="";
        String previousCloneID = "";
        String currentCloneID = "";            
        
        while ( rs.next() ) 
        {
        	currentFileName = rs.getString("file");
        	currentCloneID = rs.getString("cloneID");
        	
        	if (currentFileName.equalsIgnoreCase(previousFilename))//same file
        	{
        		//The following if condition avoids the issue of clones in the same file, only one cloneID per file is reported
        		if (!currentCloneID.equalsIgnoreCase(previousCloneID)) 
        		{
            		transaction = transaction.concat(" " + currentCloneID);
        		}           		           		
        		
        	}
        		
        	else//new file
        	{
        		if(!transaction.contentEquals(""))
        		{
        		System.out.println(transaction);
        		}
                transaction= "";                    	
            	      
                transaction = transaction.concat(currentCloneID);              
           
        	}
        	previousCloneID = currentCloneID;
            previousFilename = rs.getString("file");
            
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
