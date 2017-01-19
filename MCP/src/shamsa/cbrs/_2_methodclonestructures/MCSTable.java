package shamsa.cbrs._2_methodclonestructures;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringTokenizer;

import Utilities.Constants;
import Utilities.Database;
import yoshikihigo.jmc.JMCSearcher;
import yoshikihigo.jmc.data.DBMethod;
import yoshikihigo.jmc.data.SearcherDAO;

public class MCSTable {
	
	private static int minDepth;
	private static int support;
	private static boolean ordered;

	public static void main( String [] args ) throws Exception 
    { 
        //createMCSTable();
        //createMCSFactTable();
		//get only those MCS with depth at least
		minDepth =2;
		//support = 2;
		ordered = true;
        //displayMCSMethodsByFile(minDepth, support, ordered); 
        displayMCSMethodsByProject(minDepth, support, ordered);   

    }

	private static void displayMCSMethodsByProject(int depth2, int support2, boolean ordered2) {
		try
		{		
			ResultSet rs;			
			rs = getMCSIDs(minDepth);
			int MCSCount = 0;
			
			while (rs.next()) //iterating over MCSIDs
			{
				MCSCount += 1;
				ArrayList<Integer> clone_ids_list = new ArrayList<Integer>();  

				int MCSID = rs.getInt("MCSID");
				int sup = rs.getInt("support");
				
				printMCSIDwithSupport(MCSID, sup);
			
				clone_ids_list = getCloneIDs(MCSID);				
				
				
				ArrayList<String> projectnames_list = new ArrayList<String>(); 
				ArrayList<String> projectnames_list_deduplicated = new ArrayList<String>(); 
				
				
				int firstCloneID = clone_ids_list.get(0);
				projectnames_list = getCompleteProjectNamesList(firstCloneID);				
				projectnames_list_deduplicated = deDuplicateList(projectnames_list);
				
				
				ArrayList<String> final_project_names_list = getFinalProjectNamesList(clone_ids_list, projectnames_list_deduplicated);
				//ArrayList<String> final_projectnames_list = getFinalProjectNames(final_project_names_list);
				ArrayList<Integer> method_ids_list = new ArrayList<Integer>();  
				ArrayList<String> filenames_list_display = new ArrayList<String>(); 
				ArrayList<String> cloneIDsPlusFilenames_list = new ArrayList<String>(); 
				System.out.print("CloneID: " );
				for (int i=0; i<clone_ids_list.size(); i++)
				{
					System.out.print(" " + clone_ids_list.get(i));					
					String methodID_query = "select clones.cloneID,clones.methodID, methods.file from clones inner join methods on clones.methodID=methods.id where clones.cloneID = "+ clone_ids_list.get(i);// +" " + "//limit 1";
					Statement rs_methodIDs_stmt = Database.getInstance().getConnection().createStatement();					
					ResultSet rs_methodIDs = rs_methodIDs_stmt.executeQuery(methodID_query);
					String previousFileName = "";
					
					while (rs_methodIDs.next()) //iterating over cloneIDs
					{
						String tempFileName = rs_methodIDs.getString("file");
						String currentFileName = getProjectName(rs_methodIDs.getString("file"));
						
						String cloneIDPlusFilename = clone_ids_list.get(i) + " " + currentFileName;
						
						if(!currentFileName.contentEquals(previousFileName) && final_project_names_list.contains(currentFileName) && !cloneIDsPlusFilenames_list.contains(cloneIDPlusFilename))
								
						{		
							System.out.println(tempFileName);
							method_ids_list.add(rs_methodIDs.getInt("methodID"));
							filenames_list_display.add(currentFileName);
							cloneIDsPlusFilenames_list.add(clone_ids_list.get(i) + " " + currentFileName);
							
						}		
						previousFileName = currentFileName;
					}
					
				}
				printMethodNames(sup, method_ids_list, filenames_list_display);	
				
			}
			System.out.println("Total MCS reported: " + MCSCount);
			         
		} catch (Exception e) {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }
		
	}
	private static ArrayList<String> getFinalProjectNamesList(ArrayList<Integer> clone_ids_list,
			ArrayList<String> projectnames_list_deduplicated) throws ClassNotFoundException, SQLException {
		ArrayList<String> final_projectnames_list = new ArrayList<String>(); 
		ArrayList<String> cloneID_filenames_list = new ArrayList<String>();
		
		for(int projectNum=0; projectNum < projectnames_list_deduplicated.size(); projectNum++ )
		{
			int projectApprovalCount = 1;
			for (int i=1; i<clone_ids_list.size(); i++)
			{
				cloneID_filenames_list = getCompleteProjectNamesList(clone_ids_list.get(i));
				cloneID_filenames_list = deDuplicateList(cloneID_filenames_list);
				if(cloneID_filenames_list.contains(projectnames_list_deduplicated.get(projectNum)))
				{
					projectApprovalCount += 1;
				}
				if(projectApprovalCount == clone_ids_list.size())
					final_projectnames_list.add(projectnames_list_deduplicated.get(projectNum));
			}
			
		}
		return final_projectnames_list;
	}
	
	private static String getProjectName(String currentFileName) {
		// TODO Auto-generated method stub
		//int length = Constants.PROJECT_DIRECTORY.length()+1;
		//String trimmed = currentFileName.substring(length, currentFileName.length());
		//return trimmed.substring(0, trimmed.indexOf("\\"));	
		/*
		int length = Constants.PROJECT_DIRECTORY.length();
		String fileNameWithoutRootDirectory = currentFileName.substring(length+1, currentFileName.length());
		String fileNameTrimmedToBatchName = fileNameWithoutRootDirectory.substring(fileNameWithoutRootDirectory.indexOf("repo")+5, fileNameWithoutRootDirectory.length());
		String projectBatch = fileNameTrimmedToBatchName.substring(0, fileNameTrimmedToBatchName.indexOf("\\"));
		String fileNameTrimmedToProjectName = fileNameTrimmedToBatchName.substring(projectBatch.length()+1, fileNameTrimmedToBatchName.length());
		String projectName = fileNameTrimmedToProjectName.substring(0, fileNameTrimmedToProjectName.indexOf("\\"));
		return projectBatch + projectName;
		*/
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
	private static void displayMCSMethodsByFile(int depth, int support, boolean ordered) throws ClassNotFoundException {
		try
		{		
			ResultSet rs;			
			rs = getMCSIDs(depth);
			int MCSCount = 0;
			
			while (rs.next()) //iterating over MCSIDs
			{
				MCSCount += 1;
				ArrayList<Integer> clone_ids_list = new ArrayList<Integer>();  

				int MCSID = rs.getInt("MCSID");
				int sup = rs.getInt("support");
				
				printMCSIDwithSupport(MCSID, sup);
			
				clone_ids_list = getCloneIDs(MCSID);				
				
				
				ArrayList<String> filenames_list = new ArrayList<String>(); 
				ArrayList<String> filenames_list_deduplicated = new ArrayList<String>(); 
				
				
				int firstCloneID = clone_ids_list.get(0);
				filenames_list = getCompleteFileNamesList(firstCloneID);				
				filenames_list_deduplicated = deDuplicateList(filenames_list);
				
				
				ArrayList<String> final_filenames_list = getFinalFileNamesList(clone_ids_list, filenames_list_deduplicated);
				
				ArrayList<Integer> method_ids_list = new ArrayList<Integer>();  
				ArrayList<String> filenames_list_display = new ArrayList<String>(); 
				ArrayList<String> cloneIDsPlusFilenames_list = new ArrayList<String>(); 
				System.out.print("CloneID: " );
				for (int i=0; i<clone_ids_list.size(); i++)
				{
					System.out.print(" " + clone_ids_list.get(i));					
					String methodID_query = "select clones.cloneID,clones.methodID, methods.file from clones inner join methods on clones.methodID=methods.id where clones.cloneID = "+ clone_ids_list.get(i);// +" " + "//limit 1";
					Statement rs_methodIDs_stmt = Database.getInstance().getConnection().createStatement();					
					ResultSet rs_methodIDs = rs_methodIDs_stmt.executeQuery(methodID_query);
					String previousFileName = "";
					
					while (rs_methodIDs.next()) //iterating over cloneIDs
					{
						String currentFileName = rs_methodIDs.getString("file");
						//System.out.println(currentFileName);
						String cloneIDPlusFilename = clone_ids_list.get(i) + " " + currentFileName;
						
						if(!currentFileName.contentEquals(previousFileName))// && final_filenames_list.contains(currentFileName) && !cloneIDsPlusFilenames_list.contains(cloneIDPlusFilename))
								
						{							
							method_ids_list.add(rs_methodIDs.getInt("methodID"));
							filenames_list_display.add(currentFileName);
							cloneIDsPlusFilenames_list.add(clone_ids_list.get(i) + " " + currentFileName);
							
						}		
						previousFileName = currentFileName;
					}
					
				}
				printMethodNames(sup, method_ids_list, filenames_list_display);	
				
			}
			System.out.println("Total MCS reported: " + MCSCount);
			         
		} catch (Exception e) {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }
		
	}

	private static void printMethodNames(int sup, ArrayList<Integer> method_ids_list,
			ArrayList<String> filenames_list_display) {
		System.out.println();
		//print methods
		DBMethod dbMethod;
		String database = Constants.DATABASE;
		final SearcherDAO dao = SearcherDAO.SINGLETON;
		dao.initialize(database);
		String methodBody=null;
		
		for (int i = 0; i < method_ids_list.size(); i++) 
		{
			
			if(i % sup==0)
			{
				System.out.println("----------------------------");
			}	
			
			dbMethod = dao.getDBMethod(method_ids_list.get(i).intValue());
			methodBody = JMCSearcher.getSourcecode(dbMethod);
			String methodName = getMethodName(methodBody);
			System.out.print(methodName + " "); 
			System.out.print(filenames_list_display.get(i));
			System.out.println();
			
		}
		
		System.out.println("******************************************************************************************");
	}

	private static ArrayList<String> getFinalFileNamesList(ArrayList<Integer> clone_ids_list,
			ArrayList<String> filenames_list_deduplicated) throws SQLException, ClassNotFoundException {
		ArrayList<String> final_filenames_list = new ArrayList<String>(); 
		ArrayList<String> cloneID_filenames_list = new ArrayList<String>();
		
		for(int fileNum=0; fileNum < filenames_list_deduplicated.size(); fileNum++ )
		{
			int fileApprovalCount = 1;
			for (int i=1; i<clone_ids_list.size(); i++)
			{
				cloneID_filenames_list = getCompleteFileNamesList(clone_ids_list.get(i));
				cloneID_filenames_list = deDuplicateList(cloneID_filenames_list);
				if(cloneID_filenames_list.contains(filenames_list_deduplicated.get(fileNum)))
				{
					fileApprovalCount += 1;
				}
				if(fileApprovalCount == clone_ids_list.size())
					final_filenames_list.add(filenames_list_deduplicated.get(fileNum));
			}
			
		}
		return final_filenames_list;
	}

	private static ArrayList<String> deDuplicateList(ArrayList<String> filenames_list) {
		ArrayList<String> filenames_list_deduplicated = new ArrayList<String>();
		
		for (String file: filenames_list)
		{
			if(!filenames_list_deduplicated.contains(file))
			{
				filenames_list_deduplicated.add(file);
			}
		}
		
		return filenames_list_deduplicated;
	}

	private static ArrayList<String> getRemainingFileNames(ArrayList<Integer> clone_ids_list) throws ClassNotFoundException, SQLException {
		ArrayList<String> other_filenames_list = new ArrayList<String>();  
			
			ResultSet method_rs;		
			for (int i=1; i<clone_ids_list.size(); i++)
			{
					
				String method_query = "select clones.cloneID,clones.methodID, methods.file from clones inner join methods on clones.methodID=methods.id where clones.cloneID = "+ clone_ids_list.get(i);// +" " + "//limit 1";
				Statement method_rs_stmt = Database.getInstance().getConnection().createStatement();
				method_rs = method_rs_stmt.executeQuery(method_query);
				while (method_rs.next()) //iterating over cloneIDs
				{
					if(!other_filenames_list.contains(method_rs.getString("file")))
					{
					//method_ids_list.add(method_rs.getInt("methodID"));
					other_filenames_list.add(method_rs.getString("file"));
					}
					
				}
			}		
		
		return other_filenames_list;
	}

	private static ArrayList<String> getCompleteFileNamesList(int cloneID) throws SQLException, ClassNotFoundException {
		ArrayList<String> filenames_list = new ArrayList<String>();
		ResultSet rs_first_cloneID_files;
		String query_first_cloneID_files = "select clones.cloneID,clones.methodID, methods.file from clones inner join methods on clones.methodID=methods.id where clones.cloneID = "+ cloneID;// +" " + "//limit 1";
		Statement rs_first_cloneID_files_stmt = Database.getInstance().getConnection().createStatement();
		rs_first_cloneID_files = rs_first_cloneID_files_stmt.executeQuery(query_first_cloneID_files);
		while (rs_first_cloneID_files.next()) //iterating over cloneIDs
		{
			if(!filenames_list.contains(rs_first_cloneID_files.getString("file")))
			{
			//method_ids_list.add(rs_first_cloneID_files.getInt("methodID"));
			filenames_list.add(rs_first_cloneID_files.getString("file"));
			//filenames_list_copy.add(rs_first_cloneID_files.getString("file"));
			}
			
		}		
		return filenames_list;
	}
	
	private static ArrayList<String> getCompleteProjectNamesList(int cloneID) throws SQLException, ClassNotFoundException {
		ArrayList<String> projectnames_list = new ArrayList<String>();
		ResultSet rs_first_cloneID_projects;
		String query_first_cloneID_projects = "select clones.cloneID,clones.methodID, methods.file from clones inner join methods on clones.methodID=methods.id where clones.cloneID = "+ cloneID;// +" " + "//limit 1";
		Statement rs_first_cloneID_projects_stmt = Database.getInstance().getConnection().createStatement();
		rs_first_cloneID_projects = rs_first_cloneID_projects_stmt.executeQuery(query_first_cloneID_projects);
		while (rs_first_cloneID_projects.next()) //iterating over files
		{
			if(!projectnames_list.contains(getProjectName(rs_first_cloneID_projects.getString("file"))))
			{
			//method_ids_list.add(rs_first_cloneID_files.getInt("methodID"));
			projectnames_list.add(getProjectName(rs_first_cloneID_projects.getString("file")));
			//filenames_list_copy.add(rs_first_cloneID_files.getString("file"));
			}
			
		}		
		return projectnames_list;
	}

	private static void printMCSIDwithSupport(int MCSID, int sup) {
		System.out.print("MCSID: " + MCSID);
		System.out.println("    Support: " + sup);
	}

	private static ResultSet getMCSIDs(int depth) throws SQLException, ClassNotFoundException {
		ResultSet rs;		
		Connection conn = Database.getInstance().getConnection();
		Statement select_stmt = conn.createStatement();	
		// 1. get only those MCS with depth = 3
		String query = "select MCSID, support from method_clone_structures_facts where method_clone_structures_facts.depth >= " + minDepth + " and method_clone_structures_facts.support>=2 order by support asc";
		rs = select_stmt.executeQuery(query);
		return rs;
	}

	private static ArrayList<Integer> getCloneIDs(int MCSID) throws SQLException, ClassNotFoundException {
		Connection conn = Database.getInstance().getConnection();
		Statement stmt = conn.createStatement();
		ArrayList<Integer> clone_ids_list = new ArrayList<Integer>();  
		ResultSet clone_rs;		
		String clone_query = "select cloneID from method_clone_structures where method_clone_structures.MCSID = " + MCSID;

		clone_rs = stmt.executeQuery(clone_query);
		
		
		while (clone_rs.next())
		{
			
			clone_ids_list.add(clone_rs.getInt("cloneID"));
		}
		return clone_ids_list;
	}

	private static String getMethodName(String methodBody) {
		
		String delims = "\n";		
		StringTokenizer st = new StringTokenizer(methodBody, delims);
		String result = "";
		
		while (st.hasMoreElements()) 
		{
			result = st.nextToken().trim();
			if( (result.contains("(") && result.contains(")"))||result.endsWith("{")||(result.contains("(") && hasAccessModifer(result)) || (result.endsWith(",") && result.contains("(")))
			{	
				String methodName = result.substring(0, result.indexOf("("));
				StringTokenizer t = new StringTokenizer(methodName, " ");
			
				while (t.hasMoreElements()) 
				{					
					result = t.nextToken().trim();
				}
			
				break;
			}
		}
		return result;
 
		
	}

	private static boolean hasAccessModifer(String result) {
		if(result.contains("public")||result.contains("private")||result.contains("protected")||result.contains("void"))
			return true;
		else
			return false;
	}

	private static void createMCSFactTable() {
		try
        {
        	Class.forName("org.sqlite.JDBC");            
            Connection conn = DriverManager.getConnection(Constants.DATABASE_URL,"","");
           
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();                    
            
            int i=0;
            int mcsid=0;
 
            //create MCS table            
			stmt.executeUpdate("create table if not exists method_clone_structures_facts (id integer primary key, MCSID integer, depth integer, support integer)");		
			stmt.close();
			
			PreparedStatement insertion_stmt = conn.prepareStatement("insert into method_clone_structures_facts values (?, ?, ?,?)");
			
			//Statement insertion_stmt = conn.createStatement();
 			File f = new File(Constants.FREQUENT_ITEM_SETS_FILE);
            Scanner sc = new Scanner(f);  
            
            //read the lines into a local array
            ArrayList<String> transactions_file = new ArrayList<String>();
            while (sc.hasNextLine())
            {
            	transactions_file.add(sc.nextLine());
            }
                 
            long startTime = System.currentTimeMillis();
            // loop until all lines are read
            for(String line : transactions_file)
            {

                String[] tokenize = line.toString().split(" ");                
                mcsid+=1;
                //get the depth and support
                int depth = tokenize.length-1;
                String supportString = tokenize[tokenize.length-1];
                int support = Integer.parseInt(supportString.substring(1, supportString.length()-1));
                
                insertion_stmt.setInt(2, mcsid);
            	insertion_stmt.setInt(3, depth );
            	insertion_stmt.setInt(4, support );
                insertion_stmt.addBatch();   
             
            }
            insertion_stmt.executeBatch();

            // close file stream
            //br.close();
            conn.commit();
            insertion_stmt.close();
            stmt.close();
            sc.close();
            conn.close();
        }

        // handle exceptions
        catch (FileNotFoundException fnfe)
        {
            System.out.println("file not found");
        }

        catch (IOException ioe)
        {
            ioe.printStackTrace();
        } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
        	System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

	private static void createMCSTable() {
		try
        {
        	Class.forName("org.sqlite.JDBC");            
            Connection conn = DriverManager.getConnection(Constants.DATABASE_URL,"","");
           
            conn.setAutoCommit(false);
            Statement stmt = conn.createStatement();                    
            
            int i=0;
            int mcsid=0;
 
            //create MCS table            
			stmt.executeUpdate("create table if not exists method_clone_structures (id integer primary key, MCSID integer, cloneID integer)");		
			stmt.close();
			
			PreparedStatement insertion_stmt = conn.prepareStatement("insert into method_clone_structures values (?, ?, ?)");
			
			//Statement insertion_stmt = conn.createStatement();
 			File f = new File(Constants.FREQUENT_ITEM_SETS_FILE);
            Scanner sc = new Scanner(f);  
            
            //read the lines into a local array
            ArrayList<String> transactions_file = new ArrayList<String>();
            while (sc.hasNextLine())
            {
            	transactions_file.add(sc.nextLine());
            }
                 
            long startTime = System.currentTimeMillis();
            // loop until all lines are read
            for(String line : transactions_file)
            {

                String[] tokenize = line.toString().split(" ");                
                mcsid+=1;                
                // save each token as an MCCID or cloneID
                for (i=0; i<tokenize.length-1;i++)
                {
                	System.out.println(tokenize[i]);
                	//insertion_stmt.(1, null);
                	insertion_stmt.setInt(2, mcsid);
                	insertion_stmt.setString(3, tokenize[i] );
                    insertion_stmt.addBatch();
                	//insertion_stmt.executeUpdate("INSERT INTO method_clone_structures9 " + "VALUES (null ," +mcsid  +","+ tokenize[i] +" )");
	               
                }      
                insertion_stmt.executeBatch();
       
            }
            
            long endTime = System.currentTimeMillis();
            long elapsedTime = (endTime - startTime)/1000; //in seconds
            System.out.println("Total time required to execute SQL INSERT queries is :" + elapsedTime);
            System.out.println("MCSID is : "+ mcsid);


            // close file stream
            //br.close();
            conn.commit();
            insertion_stmt.close();
            stmt.close();
            sc.close();
            conn.close();
        }

        // handle exceptions
        catch (FileNotFoundException fnfe)
        {
            System.out.println("file not found");
        }

        catch (IOException ioe)
        {
            ioe.printStackTrace();
        } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
        	System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
