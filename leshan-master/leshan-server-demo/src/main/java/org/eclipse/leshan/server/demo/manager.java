package org.eclipse.leshan.server.demo;

import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.server.LwM2mServer;
import org.eclipse.leshan.server.demo.servlet.ClientServlet;
import org.eclipse.leshan.server.demo.servlet.json.LwM2mNodeDeserializer;
import org.eclipse.leshan.server.demo.servlet.json.LwM2mNodeSerializer;
import org.eclipse.leshan.server.demo.servlet.json.RegistrationSerializer;
import org.eclipse.leshan.server.demo.servlet.json.ResponseSerializer;
import org.eclipse.leshan.server.registration.Registration;

import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import javax.servlet.http.HttpServlet;

import com.google.gson.GsonBuilder;

public class manager {
	
	public Registration newClient;

	
	private static final long TIMEOUT = 5000; // ms
	private static LwM2mServer server;
	private static ClientServlet clientServlet ;
	
	private static HashMap<Long, List<String>> reservationStartEndList = new HashMap< Long, List<String>>();
	
    static Timer timer = new Timer();

    static class Task extends TimerTask {
        @Override
        public void run() {

        	
        	updateClientReservationStatus();
        	int delay = getNextSchedule();
            timer.schedule(new Task(), delay);

        }

    }
    
	public static void timerTask(String test) {
		clientServlet.markParkingSpotReserved(test,"LeshanClientDemo");
		
	}	
	
	public static boolean addToHash(long time, String ClientName, String CarID,String registration) {
		List<String> list_hashData = new ArrayList<String>();
		list_hashData.add(ClientName);
		list_hashData.add(CarID);
		list_hashData.add(registration);
		reservationStartEndList.put(time, list_hashData);
		return true;
	}
	
	public static long updateClientReservationStatus() {
		Long currentTime =Instant.now().getEpochSecond();
		SortedSet<Long> keys = new TreeSet<>(reservationStartEndList.keySet());
		try {
		for (Long key : keys) { 
		if(key > currentTime)	
			return 0;
		   List<String> value = reservationStartEndList.get(key);
		   if(value.get(2).equals("Start")) {
			  // System.out.println("*******------------------------************************** START VALUE:  " +value.get(2)); 
			   clientServlet.markParkingSpotReserved(value.get(1),value.get(0));
			   
			   LeshanServerSQLite.ToSQLDB("OVERVIEW",10,Instant.now().getEpochSecond(),"Active",value.get(0),"reserved",value.get(1),0,null,null);
		   }
		   else if(value.get(2).equals("End")) {
			   //System.out.println("*******------------------------************************** END VALUE:  " +value.get(2));
			   clientServlet.unmarkParkingSpotReserved(value.get(0));
			   
			   LeshanServerSQLite.ToSQLDB("OVERVIEW",10,Instant.now().getEpochSecond(),"Active",value.get(0),"free",null,0,null,null);
		   
		   }
		   System.out.println("updatingClientReservation : " +key+" "+value.get(0) + "   " + value.get(1)+"   "+value.get(2));
		  // mapItr();
		   reservationStartEndList.remove(key);
		   //mapItr();
		}
		}
		catch (Exception e) {
			System.out.println("Exception Updating Client : " +e);
		}
		
		
		
		return 0;
		
	}
	
    public static void mapItr() { // to check all data in the hashmap
        Set set = reservationStartEndList.entrySet();
        Iterator iterator = set.iterator();
        System.out.println("Map Entry Check");
        while(iterator.hasNext()) {
           Map.Entry mentry = (Map.Entry)iterator.next();
           System.out.print("key is: "+ mentry.getKey() + " & Value is: ");
           System.out.println(mentry.getValue());
        }
    }	
    public static int getNextSchedule() {
        Long currentTime =Instant.now().getEpochSecond();
        //System.out.println("Map Entry Check"); 
        System.out.println("Current Time : "+currentTime); 
        long nextKey =0;
        Set<Long> keys = reservationStartEndList.keySet();
        try {
        for(Long key: keys){
			//System.out.println("Getting Next Schedule : "+key);
			if(key < currentTime) {
				reservationStartEndList.remove(key);
			continue;
			}
			
			   if(key > nextKey){
				   if(nextKey==0)
					   nextKey = key;					 
			   }
			   else {
				   if(nextKey!=0)
					   nextKey = key;
			   }
        }
        }
        catch(Exception e) {
        	System.out.println("Error : No key to iterate: "+e);
        	
        }
        if(nextKey-currentTime <= 0)
        	return 10*1000;
        else 
        	return (int) (nextKey-currentTime)*1000;
    }
	
    public manager(LwM2mServer server, ClientServlet CS) {
       server = server;
       clientServlet = CS;
    }
	
	
	public static void manager_init(Registration newClient) {
		newClient =newClient;
		
	}


	
	public static void query_resource_status() {
//		        
//        try 
//        {
//            // create & process request
////            ReadRequest request = new ReadRequest(null, target);
////            ReadResponse cResponse = server.send(newClient, request, TIMEOUT);
////            System.out.println("***************************");
////            System.out.println(request);
////            System.out.println(cResponse);
////            System.out.println("***************************");
//        }
//		catch (RuntimeException | InterruptedException e) {
//			
//		}
	}
	
	public void print_val() {
		
	}
	
}
