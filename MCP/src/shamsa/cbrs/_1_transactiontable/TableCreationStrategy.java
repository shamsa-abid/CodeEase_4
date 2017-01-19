package shamsa.cbrs._1_transactiontable;

import java.sql.SQLException;

public interface TableCreationStrategy {
	
	public void create () throws ClassNotFoundException, SQLException;
}
