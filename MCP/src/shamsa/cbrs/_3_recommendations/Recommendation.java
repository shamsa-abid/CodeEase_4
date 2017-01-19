package shamsa.cbrs._3_recommendations;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;

import Utilities.Constants;
import yoshikihigo.jmc.JMCSearcher;
import yoshikihigo.jmc.data.*;

public class Recommendation {

	public static void main( String [] args ) 
    {
		int user_method_id = 1578;
		//int user_method_id = 74029;
		 try 
		 {
			Class.forName("org.sqlite.JDBC");
			String url = Constants.DATABASE_URL;
			//String url = "jdbc:sqlite:D:\\Downloads\\MethodClones\\JMC-master\\JMC\\newmethodclones.db";
			
			Connection conn = DriverManager.getConnection(url, "", "");
			Statement stmt = conn.createStatement();
			ResultSet rs;

			// 1. get CloneID/MCCID using MID from clones table
			String query = "select cloneID " + "from clones " + "where methodID = " + user_method_id;

			rs = stmt.executeQuery(query);
			int cloneID = 0;

			while (rs.next()) 
			{

				cloneID = rs.getInt("cloneID");
			}
	            
	        //2. get filename using MID from methods table
			String query2 = "select file " + "from methods " + "where id = " + user_method_id;

			rs = stmt.executeQuery(query2);
			String filename = "";

			while (rs.next()) 
			{
				filename = rs.getString("file");
			}
	        
			//3. get the MCSIDs using MCCID from MCS table
			String query3 = "select MCSID " + "from method_clone_structures " + "where cloneID = " + cloneID;

			rs = stmt.executeQuery(query3);
			//int MCSID = 0;
			ArrayList<Integer> MCSIDArray = new ArrayList<Integer>();

			while (rs.next()) 
			{

				MCSIDArray.add(rs.getInt("MCSID"));
			}   
	    	   
			//removing MCSes which are subsets of a major MCS
			
			
			//4. get other cloneIDs against MCSID
			ArrayList<Integer> cloneIDArray = new ArrayList<Integer>();
			for(Integer MCSID : MCSIDArray)
			{
				String query4 = "select cloneID " + "from method_clone_structures " + "where MCSID = " + MCSID + " and cloneID != "+ cloneID;
	
				rs = stmt.executeQuery(query4);
				
			    while (rs.next()) 
				{
	
			    	if(!cloneIDArray.contains(rs.getInt("cloneID")))
			    		cloneIDArray.add(rs.getInt("cloneID"));
				
				}  	 
			}
	    	    
		    //5.get the MIDs against the MCCIDs from clones table where filename matches
			//create a string parameter for the IN condition in SQL based on MCCIDs
			//String MCCIDs = String.valueOf(cloneID) + ",";
			String MCCIDs = "";
			for(Integer mccID : cloneIDArray)
			{
				MCCIDs = MCCIDs.concat(mccID + ",");
			
			}
			MCCIDs = MCCIDs.substring(0, MCCIDs.lastIndexOf(','));
			
			
		    String query5 = "select methodID,file,cloneID "+
		            "from"+
		            "(" +
		            "select clones.cloneID, clones.methodID ,methods.file " +
		            "from clones " +
		            "left join methods " +
		            "on clones.methodID = methods.id " +
		            "where methods.file is not NULL " +
		            //"order by methods.file " +
		            ") as t " +
		            "where file = '"+filename +"' and cloneID IN ("  + MCCIDs +  ");" 
		            ;

			rs = stmt.executeQuery(query5);
			ArrayList<Integer> methodIDArray = new ArrayList<Integer>();
		    while (rs.next()) 
			{

				methodIDArray.add(rs.getInt("methodID"));
			
			} 
		    
		    //6. display the original method and recommended methods
		    //Display Original Method
		    //String database = "D:\\Downloads\\MethodClones\\JMC-master\\JMC\\newmethodclones.db";
		    String database = Constants.DATABASE;
		    final SearcherDAO dao = SearcherDAO.SINGLETON;
			dao.initialize(database);
			String methodBody=null;
			
			System.out.println("User method ID: " + user_method_id);
	    	
	    	DBMethod dbMethod = dao.getDBMethod(user_method_id);
	    	methodBody = JMCSearcher.getSourcecode(dbMethod);
	    	System.out.print(methodBody);
	    	System.out.println("=================================================================");
			
		    //Display Recommended Methods
			
		    for(Integer MID : methodIDArray)
		    {
		    	System.out.println(MID);
		    	
		    	dbMethod = dao.getDBMethod(MID.intValue());
		    	methodBody = JMCSearcher.getSourcecode(dbMethod);
		    	System.out.print(methodBody);
		    }
		    
	            conn.close();
	        } catch (Exception e) {
	            System.err.println("Got an exception! ");
	            System.err.println(e.getMessage());
	        }
		
     
    }
}
