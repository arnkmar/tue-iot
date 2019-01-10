package org.eclipse.leshan.server.demo;

import java.sql.*;



public class LeshanServerSQLite {

	   public static void create(int code, String tablename) {
		      Connection c = null;
		      Statement stmt = null;
		      
		     String VehicleReg = "CREATE TABLE REGISTERED_VEHICLES (VEHID TEXT, LICPLNUM TEXT);"; 
		     String Overview = "CREATE TABLE OVERVIEW ( TIME BIGINT NOT NULL,  EVENT TEXT,PIID TEXT PRIMARY KEY, STATE TEXT, CARNUMBER TEXT);";
		     //String RESERVATION_H24 ="CREATE TABLE RESERVATION (PIID TEXT PRIMARY KEY NOT NULL, H1 TEXT, H2 TEXT, H3 TEXT,H4 TEXT, H5 TEXT, H6 TEXT, H7 TEXT, H8 TEXT, H9 TEXT,H10 TEXT, H11 TEXT, H12 TEXT, H13 TEXT, H14 TEXT, H15 TEXT,H16 TEXT, H17 TEXT, H18 TEXT, H19 TEXT, H20 TEXT, H21 TEXT,H22 TEXT, H23 TEXT, H24 TEXT);";
		     String RESERVATION_Table_per_ParkingLot = "CREATE TABLE "+tablename+" (TIME BIGINT PRIMARY KEY NOT NULL, RSTART BIGINT, REND BIGINT, RCAR TEXT, PSTART BIGINT, PEND BIGINT, PCAR TEXT, VALID BIGINT);";
	         String IoTparking_all_events = "CREATE TABLE IoTParking " +
     		 		"(TIME BIGINT PRIMARY KEY   NOT NULL," +
     		 		" PIID TEXT 			     ," +
     		 		" EVENT TEXT 			     ," +
                     " OCCUPANCY           TEXT    , " + 
                     " OCCUPIEDCARNO            TEXT, " +
                     " CONFIDENCECARNO            REAL, " +
                     " PATHTOFILE        TEXT, " + 
                     " RESERVEDFOR        TEXT )"; 

	         String sql = null;

	         

		     
		      try {
		         Class.forName("org.sqlite.JDBC");
		         c = DriverManager.getConnection("jdbc:sqlite:IoTParking.db");
			     if(code == 1)
			    	 sql = RESERVATION_Table_per_ParkingLot;
			     else if(code == 2)
			    	 sql =Overview;
			     else if(code == 3)
			    	 sql =IoTparking_all_events;
			     else if(code == 4)
			    	 sql =VehicleReg;
			     
			     System.out.println(sql);
  
		         System.out.println("Opened database successfully");

		         stmt = c.createStatement();
		         


		
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
	   
	   public static void ToSQLDB(String Tablename,int code, long time,String event, String piid, String Occupancy, String OccuCarID, double confidence, String pathToFile, String ReservedFor) throws SQLException {
/*		   
		   this.time = time;
		   this.piid=piid;
		   this.Occupany=Occupancy;
		   this.OccuCarID=OccuCarID;
		   this.confidence=confidence;
		   this.pathToFile=pathToFile;
		   this.ReservedFor=ReservedFor;
*/		   
if(code == 1) // IoTParking
{
			   String str = "INSERT INTO "+Tablename+" (TIME,EVENT,PIID,OCCUPANCY,OCCUPIEDCARNO,CONFIDENCECARNO,PATHTOFILE,RESERVEDFOR) VALUES (" 
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
else if (code ==10) // OVERVIEW-TABLE
{
	
	
	   String str = "INSERT INTO "+Tablename+" (TIME, EVENT,PIID , STATE , CARNUMBER ) VALUES (" 
	   + Long.toString(time)
	   + ",'"+event
	   +"','"+ piid
	   + "','"+Occupancy
	   +"','"+OccuCarID
	   + "');" ;
		System.out.println(str);
	   boolean success = insert(str);
	   
	   if (!success)
	   {
		   str = "UPDATE "+Tablename+" SET TIME="+ Long.toString(time)
		   		+",EVENT='"+event
		   		+"',STATE='"+Occupancy
		   		+"',CARNUMBER='"+OccuCarID
		   		+"' WHERE PIID='"+piid
		   		+ "';" ;
					System.out.println(str);
				   update(str);
	   }
	   
	   
}

else if(code ==30) { // Update Spot table for car exit
	String sql = "UPDATE "+Tablename+" SET PEND="+ Long.toString(time) +" WHERE PEND IS NULL AND PSTART NOT NULL;";
	update(sql);
}
else if(code ==31) { // Update Spot table for car entry 

	String sql = "insert into "+Tablename+" (time,pstart,pcar) values ("+Long.toString(time)+","+Long.toString(time)+",'"+OccuCarID+"');";
	insert(sql);
	System.out.println(sql);
}



		   
		   
		   
		   
		   
	   }
	   

	   
	   public static boolean insert(String sql) throws SQLException {
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
		         if (e.getMessage().contains("UNIQUE constraint failed:")) 
		         {  stmt.close(); c.close(); return false; }
		         
		         
		         
		      }
		      System.out.println("Records created successfully");
		      return true;
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
	   
	   public static boolean hit(String ID) {

		   
		   

				String sql="SELECT VEHID FROM REGISTERED_VEHICLES WHERE REGISTERED_VEHICLES.VEHID='"+ID+"';";

		   
		   Connection c = null;
		   Statement stmt = null;
		   try {
		      Class.forName("org.sqlite.JDBC");
		      c = DriverManager.getConnection("jdbc:sqlite:IoTParking.db");
		      c.setAutoCommit(false);
		      System.out.println("Opened database successfully");

		      stmt = c.createStatement();
		      ResultSet rs = stmt.executeQuery( sql );
		      
		      while ( rs.next() ) {
		         
		         String  piid = rs.getString("VEHID");

		         
		         System.out.println( "VEHID = "+ piid  );
		 

		         System.out.println(rs);
		         return false;
		      }
		      rs.close();
		      stmt.close();
		      c.close();
		      
		   } catch ( Exception e ) {
		      System.err.println( e.getClass().getName() + ": " + e.getMessage() );
		      System.exit(0);
		   }
		   System.out.println("Operation-HIT done successfully");
		   return true;
		  }
	   
	   
	   public static void update(String sql ) {
		   
		   Connection c = null;
		   Statement stmt = null;
		   
		   try {
		      Class.forName("org.sqlite.JDBC");
		      c = DriverManager.getConnection("jdbc:sqlite:IoTParking.db");
		      c.setAutoCommit(false);
		      System.out.println("Opened database successfully");
		    //  set = " SALARY = 25000.00"; 
		    //  where = " ID=1";
		      stmt = c.createStatement();
		      
		      
		      System.out.println(sql);
		      //sql = "UPDATE COMPANY SET ADDRESS='hello' WHERE ID=1";
		      System.out.println(sql);
		      stmt.executeUpdate(sql);
		      c.commit();

//		      ResultSet rs = stmt.executeQuery( "SELECT * FROM COMPANY;" );
		      
//		      while ( rs.next() ) {
//		         int id = rs.getInt("id");
//		         String  name = rs.getString("name");
//		         int age  = rs.getInt("age");
//		         String  address = rs.getString("address");
//		         float salary = rs.getFloat("salary");
//		         
//		         System.out.println( "ID = " + id );
//		         System.out.println( "NAME = " + name );
//		         System.out.println( "AGE = " + age );
//		         System.out.println( "ADDRESS = " + address );
//		         System.out.println( "SALARY = " + salary );
//		         System.out.println();
//		      }
//		      rs.close();
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
	   
	   
	   public static String userToDB (int code, int start, int end ) {
		   
		  String ret = null;
		   
		  Connection connection = null;
		  Statement stmt = null;
		  
		  try {
			  // Load the MySQL JDBC driver
			  Class.forName("org.sqlite.JDBC");
			  // Create a connection to the database
			  connection = DriverManager.getConnection("jdbc:sqlite:IoTParking.db");
			  System.out.println("Successfully Connected to the database!");
			  } 
			  catch (ClassNotFoundException e) {
			  System.out.println("Could not find the database driver " + e.getMessage());
			  } 
			  catch (SQLException e) {
			  System.out.println("Could not connect to the database " + e.getMessage());
			  }

		  try {
	
			  // Get the database metadata
			  DatabaseMetaData metadata = connection.getMetaData();
			  // Specify the type of object; in this case we want tables
			  String[] types = {"TABLE"};
			  ResultSet resultSet = metadata.getTables(null, null, "%", types);
	
			  while (resultSet.next()) {			   
			    String tableName = resultSet.getString(3);   
			    System.out.println("Checking table " + tableName );
				   if(code==1) { // check for clashing reservation for user requested time
					   String sql = 
							    "SELECT  *" + 
								" FROM "+tableName+" r " + 
								" WHERE ("+Integer.toString(start)+" BETWEEN r.RSTART AND r.REND) " + 
								" OR ("+Integer.toString(end)+" BETWEEN r.RSTART AND r.REND) " + 
								" OR ("+Integer.toString(start)+" <= r.RSTART AND "+Integer.toString(end)+" >= r.REND);";
					   System.out.println(sql);			
						try {
							stmt = connection.createStatement();	
						      ResultSet rs = stmt.executeQuery( sql);
						      System.out.println(rs);
						      
						      if ( rs.next() ) { // there is a blocking reservation
						         System.out.println("there is a blocking reservation");
						      }
						      else { // the spot is available for user's choice
						    	  if(!(tableName.equals("IoTParking")||tableName.equals("OVERVIEW")))
								    System.out.println("Table : " + tableName );
						    	  	ret = ret +","+ tableName  ;
						      }
						      rs.close();
									
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					
				   }		    
			  }
		  } 
		  catch (SQLException e) {

		  System.out.println("Could not get database metadata " + e.getMessage());
		  }
		   return ret;
		   
	   }
	      
}


