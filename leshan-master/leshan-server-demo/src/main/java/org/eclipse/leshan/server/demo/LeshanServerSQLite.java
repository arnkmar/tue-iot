package org.eclipse.leshan.server.demo;

import java.sql.*;

public class LeshanServerSQLite {

	   public static void create() {
		      Connection c = null;
		      Statement stmt = null;
		      
		      try {
		         Class.forName("org.sqlite.JDBC");
		         c = DriverManager.getConnection("jdbc:sqlite:IoTParking.db");
		         System.out.println("Opened database successfully");

		         stmt = c.createStatement();
				         String sql = "CREATE TABLE IoTParking " +
			        		 		"(TIME BIGINT PRIMARY KEY   NOT NULL," +
			        		 		" PIID TEXT 			     ," +
			        		 		" EVENT TEXT 			     ," +
			                        " OCCUPANCY           TEXT    , " + 
			                        " OCCUPIEDCARNO            TEXT, " +
			                        " CONFIDENCECARNO            REAL, " +
			                        " PATHTOFILE        TEXT, " + 
			                        " RESERVEDFOR        TEXT )"; 
				         System.out.println(sql);
		         stmt.executeUpdate(sql);
		         stmt.close();
		         c.close();
		      } catch ( Exception e ) {
		         System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		         //System.exit(0);
		         return;
		      }
		      System.out.println("Table created successfully");
		   }
	   
	   public static void ToSQLDB(int operation, long time,String event, String piid, String Occupancy, String OccuCarID, double confidence, String pathToFile, String ReservedFor) {
/*		   
		   this.time = time;
		   this.piid=piid;
		   this.Occupany=Occupancy;
		   this.OccuCarID=OccuCarID;
		   this.confidence=confidence;
		   this.pathToFile=pathToFile;
		   this.ReservedFor=ReservedFor;
*/		   

			   String str = "INSERT INTO IoTParking (TIME,EVENT,PIID,OCCUPANCY,OCCUPIEDCARNO,CONFIDENCECARNO,PATHTOFILE,RESERVEDFOR) VALUES (" 
			   + Long.toString(time)
			   + ",'"+event
			   +"','"+ piid
			   + "','"+Occupancy
			   +"','"+OccuCarID
			   +"',"+ Double.toString(confidence)
			   +",'"+pathToFile
			   +"','"+ReservedFor
			   + "');" ;
		 		System.out.println(str);
			   insert(str);
			   
			   
		   
		   
		   
		   
		   
	   }
	   
	   public static void insert(String sql) {
		      Connection c = null;
		      Statement stmt = null;
		      
		      try {
		         Class.forName("org.sqlite.JDBC");
		         c = DriverManager.getConnection("jdbc:sqlite:IoTParking.db");
		         c.setAutoCommit(false);
		         System.out.println("Opened database successfully");

		         stmt = c.createStatement(); 
//		         stmt.executeUpdate(sql);

//		         String sql = "INSERT INTO COMPANY (ID,NAME,AGE,ADDRESS,SALARY) " +
	                        //"VALUES (1, 'Paul', 325, 'California', 200500.00 );"; 

	                        stmt.executeUpdate(sql);


		         
		         stmt.close();
		         c.commit();
		         c.close();
		      } catch ( Exception e ) {
		         System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		         System.exit(0);
		      }
		      System.out.println("Records created successfully");
		   }
	   
	   public static void select() {

		   Connection c = null;
		   Statement stmt = null;
		   try {
		      Class.forName("org.sqlite.JDBC");
		      c = DriverManager.getConnection("jdbc:sqlite:IoTParking.db");
		      c.setAutoCommit(false);
		      System.out.println("Opened database successfully");

		      stmt = c.createStatement();
		      ResultSet rs = stmt.executeQuery( "SELECT * FROM IoTParking;" );
		      
		      while ( rs.next() ) {
		         long time = rs.getLong("time");
		         String  piid = rs.getString("piid");

		         
		         System.out.println( "ID = " + time );
		         System.out.println( "NAME = " + piid );

		         System.out.println();
		      }
		      rs.close();
		      stmt.close();
		      c.close();
		   } catch ( Exception e ) {
		      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		      System.exit(0);
		   }
		   System.out.println("Operation-SELECT done successfully");
		  }
	   
	   public static void update(String set, String where ) {
		   
		   Connection c = null;
		   Statement stmt = null;
		   
		   try {
		      Class.forName("org.sqlite.JDBC");
		      c = DriverManager.getConnection("jdbc:sqlite:test_static.db");
		      c.setAutoCommit(false);
		      System.out.println("Opened database successfully");
		    //  set = " SALARY = 25000.00"; 
		    //  where = " ID=1";
		      stmt = c.createStatement();
		      String sql = (String) "UPDATE COMPANY SET "+set+" WHERE"+ where;
		      
		      System.out.println(sql);
		      sql = "UPDATE COMPANY SET ADDRESS='hello' WHERE ID=1";
		      System.out.println(sql);
		      stmt.executeUpdate(sql);
		      c.commit();

		      ResultSet rs = stmt.executeQuery( "SELECT * FROM COMPANY;" );
		      
		      while ( rs.next() ) {
		         int id = rs.getInt("id");
		         String  name = rs.getString("name");
		         int age  = rs.getInt("age");
		         String  address = rs.getString("address");
		         float salary = rs.getFloat("salary");
		         
		         System.out.println( "ID = " + id );
		         System.out.println( "NAME = " + name );
		         System.out.println( "AGE = " + age );
		         System.out.println( "ADDRESS = " + address );
		         System.out.println( "SALARY = " + salary );
		         System.out.println();
		      }
		      rs.close();
		      stmt.close();
		      c.close();
		   } catch ( Exception e ) {
		      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		      System.exit(0);
		   }
		    System.out.println("Operation-UPDATE done successfully");
		   }
	   
	   public static void delete( ) {
		      Connection c = null;
		      Statement stmt = null;
		      
		      try {
		         Class.forName("org.sqlite.JDBC");
		         c = DriverManager.getConnection("jdbc:sqlite:test_static.db");
		         c.setAutoCommit(false);
		         System.out.println("Opened database successfully");

		         stmt = c.createStatement();
		         String sql = "DELETE FROM COMPANY;";
		         stmt.executeUpdate(sql);
		         c.commit();


		      stmt.close();
		      c.close();
		      } catch ( Exception e ) {
		         System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		         System.exit(0);
		      }
		      System.out.println("Operation-DELETE done successfully");
		   }
	   
	   
}


