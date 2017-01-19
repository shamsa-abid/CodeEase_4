package shamsa.cbrs._1_transactiontable;
import java.sql.*;

import Utilities.Constants;
import Utilities.Database;

import java.io.*;

public class TransactionTable {
	
	private String type;
	private TableCreationStrategy tableCreationStrategy;
	
	private TransactionTable()
	{
		
	}
	private void createTransactionTable() throws ClassNotFoundException, SQLException
	{
		tableCreationStrategy.create();
	}
	public static void main (String[] args) {
        try 
        {
        	TransactionTable tt = new TransactionTable(); 
        	//tt.setTableCreationStrategy(new FileStrategy());
        	tt.setTableCreationStrategy(new ProjectStrategy());
        	tt.createTransactionTable();       	
           
        } catch (Exception e) {
            System.err.println("Got an exception! ");
            System.err.println(e.getMessage());
        }
    }

	

	public TableCreationStrategy getTableCreationStrategy() {
		return tableCreationStrategy;
	}

	public void setTableCreationStrategy(TableCreationStrategy tableCreationStrategy) {
		this.tableCreationStrategy = tableCreationStrategy;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}
