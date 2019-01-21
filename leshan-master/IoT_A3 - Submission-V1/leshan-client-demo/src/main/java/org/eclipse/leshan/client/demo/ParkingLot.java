package org.eclipse.leshan.client.demo;

import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Scanner;

import org.apache.commons.lang.StringUtils;
import org.eclipse.leshan.client.resource.BaseInstanceEnabler;
import org.eclipse.leshan.core.model.ObjectModel;
import org.eclipse.leshan.core.node.LwM2mResource;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.eclipse.leshan.util.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParkingLot extends BaseInstanceEnabler {
	
    private static final Logger LOG = LoggerFactory.getLogger(ParkingLot.class);
    
    private static final int SPOT_ID = 32800;
    private static final int SPOT_STATE_ID = 32801;
    private static final int VEHICLE = 32802;
    private static final int BILLING_RATE_ID = 32803;
    private static final int RESERVATION_ID = 32804;
    
    
    private static final List<Integer> supportedResources = Arrays.asList(SPOT_ID,SPOT_STATE_ID,VEHICLE,BILLING_RATE_ID);
    
    private String PARKING_SPOT_ID = "Parking-Spot-4";
    private String PARKING_SPOT_STATE = "free";
    private boolean RESERVATION=false;
    private String VEHICLE_ID=null;
    private double BILLING_RATE=0.01; 
    private float CONFIDENCE;
   
    public ParkingLot() {
    	
        // notify new date each 5 second
        Timer timer = new Timer("Device-Current Time");
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                fireResourcesChange(32801);
                getParkingSpotState();
            }
        }, 5000, 5000);
    }

    @Override
    public synchronized ReadResponse read(int resourceId) {
        switch (resourceId) {
        case SPOT_ID:
            return ReadResponse.success(resourceId, PARKING_SPOT_ID);
        case SPOT_STATE_ID:
            return ReadResponse.success(resourceId, PARKING_SPOT_STATE);
        case VEHICLE:
            return ReadResponse.success(resourceId, VEHICLE_ID);
        case RESERVATION_ID:
            return ReadResponse.success(resourceId, RESERVATION );
        case BILLING_RATE_ID:
            return ReadResponse.success(resourceId, BILLING_RATE);
            
        default:
            return super.read(resourceId);
        }
    }

    @Override
    public synchronized ExecuteResponse execute(int resourceId, String params) {
        switch (resourceId) {
        case BILLING_RATE_ID: ;
            return ExecuteResponse.success();
        default:
            return super.execute(resourceId, params);
        }
    }
    
    @Override
    public WriteResponse write(int resourceid, LwM2mResource value) {
        LOG.info("Write on Device Resource " + resourceid + " value " + value);
        switch (resourceid) { 
        case BILLING_RATE_ID:
        	BILLING_RATE = (double) value.getValue();
            fireResourcesChange(resourceid);
            return WriteResponse.success();
            
        case RESERVATION_ID: 
        	String Val = (String) value.getValue();
            fireResourcesChange(resourceid);
            if(Val == "0")
            {
            	PARKING_SPOT_STATE = "Free";
                RESERVATION=false;
            }
            else 
            {
            	RESERVATION = true;
            	PARKING_SPOT_STATE = "reserved";
            	VEHICLE_ID = Val;
            }
            return WriteResponse.success();

            
        case VEHICLE: 
        	String Val1 = (String) value.getValue();
            VEHICLE_ID =Val1;
        	fireResourcesChange(resourceid);
            return WriteResponse.success();
            
        case SPOT_STATE_ID: 
        	String Val2 = (String) value.getValue();
            PARKING_SPOT_STATE =Val2;
        	fireResourcesChange(resourceid);
            return WriteResponse.success();
           
            
        default:
            return super.write(resourceid, value);
        }
    }
    
    class occupancy 
    {
    	public String CarLicencePlate;
    	public float confidence;
    };
    
    public void getParkingSpotState() {
    	String spot_dir = "/spot_occupied";
        
        File tmpDir = new File(System.getProperty("user.dir")+spot_dir);
        boolean exists = tmpDir.exists();
        
        if(exists) 
        {
        	PARKING_SPOT_STATE = "occupied";
        	occupancy occ = new occupancy();
        	splitOccupancyData(tmpDir,occ);       	
        	VEHICLE_ID = occ.CarLicencePlate;
        }
        else 
        {	if(PARKING_SPOT_STATE.equals("occupied"))
        	PARKING_SPOT_STATE = "free";	
        }
        
    }
    
    private void splitOccupancyData(File tmpDir, occupancy tmp) { 
    	File[] listOfFiles = tmpDir.listFiles();
    	
    	try {
    	for (int i = 0; i < listOfFiles.length; i++) {

    		if (listOfFiles[i].isDirectory()) {
    	    String[] parts = listOfFiles[i].getName().split("=");
    	    tmp.CarLicencePlate=parts[0];
    	    tmp.confidence=Float.valueOf(parts[1]);  
    	  }
    	  
    	}
    	}
    	catch (Exception e){
    		
    		System.out.println("ERROR " );
    	}
    }
    @Override
    public List<Integer> getAvailableResourceIds(ObjectModel model) {
        return supportedResources;
    }
}
