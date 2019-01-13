/*******************************************************************************
 * Copyright (c) 2013-2015 Sierra Wireless and others.
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution.
 * 
 * The Eclipse Public License is available at
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at
 *    http://www.eclipse.org/org/documents/edl-v10.html.
 * 
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *******************************************************************************/
package org.eclipse.leshan.server.demo.servlet;


import java.io.IOException;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.eclipse.californium.core.network.Endpoint;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.observation.Observation;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.server.californium.impl.LeshanServer;
import org.eclipse.leshan.server.demo.LeshanServerDemo;
import org.eclipse.leshan.server.demo.LeshanServerSQLite;
import org.eclipse.leshan.server.demo.manager;
import org.eclipse.leshan.server.demo.servlet.json.LwM2mNodeSerializer;
import org.eclipse.leshan.server.demo.servlet.json.RegistrationSerializer;
import org.eclipse.leshan.server.demo.servlet.log.CoapMessage;
import org.eclipse.leshan.server.demo.servlet.log.CoapMessageListener;
import org.eclipse.leshan.server.demo.servlet.log.CoapMessageTracer;
import org.eclipse.leshan.server.demo.utils.EventSource;
import org.eclipse.leshan.server.demo.utils.EventSourceServlet;
import org.eclipse.leshan.server.observation.ObservationListener;
import org.eclipse.leshan.server.queue.PresenceListener;
import org.eclipse.leshan.server.registration.Registration;
import org.eclipse.leshan.server.registration.RegistrationListener;
import org.eclipse.leshan.server.registration.RegistrationUpdate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


import java.util.Map;
import java.util.Iterator;
import java.util.Set;



public class EventServlet extends EventSourceServlet {

    private static final String EVENT_DEREGISTRATION = "DEREGISTRATION";

    private static final String EVENT_UPDATED = "UPDATED";

    private static final String EVENT_REGISTRATION = "REGISTRATION";

    private static final String EVENT_AWAKE = "AWAKE";

    private static final String EVENT_SLEEPING = "SLEEPING";

    private static final String EVENT_NOTIFICATION = "NOTIFICATION";

    private static final String EVENT_COAP_LOG = "COAPLOG";

    private static final String QUERY_PARAM_ENDPOINT = "ep";

    private static final long serialVersionUID = 1L;

    private static final Logger LOG = LoggerFactory.getLogger(EventServlet.class);

    private final Gson gson;

    private final CoapMessageTracer coapMessageTracer;
    
    HashMap<String, String > parkingLotoccupancyMap = new HashMap< String,String>();

    private Set<LeshanEventSource> eventSources = Collections
            .newSetFromMap(new ConcurrentHashMap<LeshanEventSource, Boolean>());

    private final RegistrationListener registrationListener = new RegistrationListener() {
    	
    	

        @Override
        public void registered(Registration registration, Registration previousReg,
                Collection<Observation> previousObsersations) {
            String jReg = EventServlet.this.gson.toJson(registration);
            System.out.println("registration");   
            
            try { // Get initial values from client and store to database | Also create a map
            	String occupancyValue =ClientServlet.getResource(registration, 1); // also updates Overview table
				ClientServlet.startObservation(registration,occupancyValue );
				LeshanServerSQLite.create(1,registration.getEndpoint()); // table for registration service
				LeshanServerSQLite.ToSQLDB("IoTParking",1,Instant.now().getEpochSecond(),"Registration",registration.getEndpoint(),null,null,0,null,null );
				LeshanServerSQLite.ToSQLDB(registration.getEndpoint(),30,0,"CarExit",registration.getEndpoint(),"UNKNOWN",null,0,null,null );
				
				parkingLotoccupancyMap.put(registration.getEndpoint(), occupancyValue );
				mapItr();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            sendEvent(EVENT_REGISTRATION, jReg, registration.getEndpoint());
            
        }

      
        @Override
        public void updated(RegistrationUpdate update, Registration updatedRegistration,
                Registration previousRegistration) {
            String jReg = EventServlet.this.gson.toJson(updatedRegistration);
            System.out.println("updated");
            sendEvent(EVENT_UPDATED, jReg, updatedRegistration.getEndpoint());
        }
        
        public void mapItr() {
            Set set = parkingLotoccupancyMap.entrySet();
            Iterator iterator = set.iterator();
            System.out.println("Map Entry Check");
            while(iterator.hasNext()) {
               Map.Entry mentry = (Map.Entry)iterator.next();
               System.out.print("key is: "+ mentry.getKey() + " & Value is: ");
               System.out.println(mentry.getValue());
            }
        }

        @Override
        public void unregistered(Registration registration, Collection<Observation> observations, boolean expired,
                Registration newReg) {
            String jReg = EventServlet.this.gson.toJson(registration);
            System.out.println("unregistration");
            try {
				LeshanServerSQLite.ToSQLDB("IoTParking",1,Instant.now().getEpochSecond(),"De-registration",registration.getEndpoint(),null,null,0,null,null );
				LeshanServerSQLite.ToSQLDB("OVERVIEW",10,Instant.now().getEpochSecond(),"INACTIVE",registration.getEndpoint(),null,null,0,null,null );
				parkingLotoccupancyMap.remove(registration.getEndpoint());
				mapItr();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            sendEvent(EVENT_DEREGISTRATION, jReg, registration.getEndpoint());
        }

    };

    public final PresenceListener presenceListener = new PresenceListener() {

        @Override
        public void onSleeping(Registration registration) {
            String data = new StringBuilder("{\"ep\":\"").append(registration.getEndpoint()).append("\"}").toString();

            sendEvent(EVENT_SLEEPING, data, registration.getEndpoint());
        }

        @Override
        public void onAwake(Registration registration) {
            String data = new StringBuilder("{\"ep\":\"").append(registration.getEndpoint()).append("\"}").toString();
            sendEvent(EVENT_AWAKE, data, registration.getEndpoint());
        }
    };

    private final ObservationListener observationListener = new ObservationListener() {

        @Override
        public void cancelled(Observation observation) {
        }

        @Override
        public void onResponse(Observation observation, Registration registration, ObserveResponse response) {
        	long time_now = Instant.now().getEpochSecond();
        	if (LOG.isDebugEnabled()) {
                LOG.debug("Received notification from [{}] containing value [{}]", observation.getPath(),
                        response.getContent().toString());
            }

            if (registration != null) {
                String data = new StringBuilder("{\"ep\":\"").append(registration.getEndpoint()).append("\",\"res\":\"")
                        .append(observation.getPath().toString()).append("\",\"val\":")
                        .append(gson.toJson(response.getContent())).append("}").toString();
                sendEvent(EVENT_NOTIFICATION, data, registration.getEndpoint());
                System.out.println("Onresponse Obervation");
                
               // System.out.println(observation.getPath().toString());
                
                if(observation.getPath().toString().equals("/32700/0/32801")) // snoop status of parkingSPot occupancy
                {
                	//System.out.println("EventServlet->onResponse-observation : "+response.getContent()+"");
                	String[] path = StringUtils.split(response.getContent().toString(), ',');
                	String[] occupancy = StringUtils.split(path[1], '=');
                	
                	if(occupancy[1].toLowerCase().equals(parkingLotoccupancyMap.get(registration.getEndpoint())))
                			{
                        //System.out.println("Observation : Resource value - SAME -"+registration.getEndpoint());
                        //System.out.println("Observation :"+occupancy[1]+" "+parkingLotoccupancyMap.get(registration.getEndpoint()));
                	}
                	else {
                		
                		parkingLotoccupancyMap.replace(registration.getEndpoint(), occupancy[1].toLowerCase() );
                		//System.out.println("Observation : Resource value - CNGE -"+registration.getEndpoint());
                		//System.out.println("Observation :"+occupancy[1]+" "+parkingLotoccupancyMap.get(registration.getEndpoint()));
                		if(occupancy[1].equals("occupied")) {
                		try {
							String carID =ClientServlet.getResource(registration, 2); // get car ID with code 2
							String rate= ClientServlet.getResource(registration, 3); // get parking rate with code 3							
							System.out.println("Observation RATE ***** :"+rate);
							LeshanServerSQLite.ToSQLDB("OVERVIEW",10,time_now,"Active",registration.getEndpoint(),occupancy[1].toLowerCase(),carID,0,null,null );
							LeshanServerSQLite.ToSQLDB("IoTParking",1,time_now,"Car-Entry",registration.getEndpoint(),occupancy[1].toLowerCase(),carID,0,null,null );
							LeshanServerSQLite.ToSQLDB(registration.getEndpoint(),31,time_now,"CarEntry",registration.getEndpoint(),occupancy[1].toLowerCase(),carID,Float.parseFloat(rate),null,null );
						} catch (SQLException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
                		}
                		else if(occupancy[1].equals("free"))
                		{
                			try {
								LeshanServerSQLite.ToSQLDB("OVERVIEW",10,time_now,"Active",registration.getEndpoint(),occupancy[1].toLowerCase(),null,0,null,null );
								LeshanServerSQLite.ToSQLDB(registration.getEndpoint(),30,time_now,"CarExit",registration.getEndpoint(),occupancy[1].toLowerCase(),null,0,null,null );
								LeshanServerSQLite.ToSQLDB("IoTParking",1,time_now,"Car-Exit",registration.getEndpoint(),occupancy[1].toLowerCase(),null,0,null,null );

								
								
							} catch (SQLException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                		}
                		
                	}
                }
                		
                
                


              
            }
        }

        @Override
        public void onError(Observation observation, Registration registration, Exception error) {
            if (LOG.isWarnEnabled()) {
                LOG.warn(String.format("Unable to handle notification of [%s:%s]", observation.getRegistrationId(),
                        observation.getPath()), error);
            }
        }

        @Override
        public void newObservation(Observation observation, Registration registration) {
        }
    };

    public EventServlet(LeshanServer server, int securePort) {
        server.getRegistrationService().addListener(this.registrationListener);
        server.getObservationService().addListener(this.observationListener);
        server.getPresenceService().addListener(this.presenceListener);

        // add an interceptor to each endpoint to trace all CoAP messages
        coapMessageTracer = new CoapMessageTracer(server.getRegistrationService());
        for (Endpoint endpoint : server.coap().getServer().getEndpoints()) {
            endpoint.addInterceptor(coapMessageTracer);
        }

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(Registration.class,
                new RegistrationSerializer(server.getPresenceService()));
        gsonBuilder.registerTypeHierarchyAdapter(LwM2mNode.class, new LwM2mNodeSerializer());
        gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        this.gson = gsonBuilder.create();
    }

    private synchronized void sendEvent(String event, String data, String endpoint) {
        if (LOG.isDebugEnabled()) {
            LOG.debug("Dispatching {} event from endpoint {}", event, endpoint);
        }

        for (LeshanEventSource eventSource : eventSources) {
            if (eventSource.getEndpoint() == null || eventSource.getEndpoint().equals(endpoint)) {
                eventSource.sentEvent(event, data);
            }
        }
    }

    class ClientCoapListener implements CoapMessageListener {

        private final String endpoint;

        ClientCoapListener(String endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public void trace(CoapMessage message) {
            String coapLog = EventServlet.this.gson.toJson(message);
            sendEvent(EVENT_COAP_LOG, coapLog, endpoint);
        }

    }

    private void cleanCoapListener(String endpoint) {
        // remove the listener if there is no more eventSources for this endpoint
        for (LeshanEventSource eventSource : eventSources) {
            if (eventSource.getEndpoint() == null || eventSource.getEndpoint().equals(endpoint)) {
                return;
            }
        }
        coapMessageTracer.removeListener(endpoint);
    }

    @Override
    protected EventSource newEventSource(HttpServletRequest req) {
        String endpoint = req.getParameter(QUERY_PARAM_ENDPOINT);
        return new LeshanEventSource(endpoint);
    }

    private class LeshanEventSource implements EventSource {

        private String endpoint;
        private Emitter emitter;

        public LeshanEventSource(String endpoint) {
            this.endpoint = endpoint;
        }

        @Override
        public void onOpen(Emitter emitter) throws IOException {
            this.emitter = emitter;
            eventSources.add(this);
            if (endpoint != null) {
                coapMessageTracer.addListener(endpoint, new ClientCoapListener(endpoint));
            }
        }

        @Override
        public void onClose() {
            cleanCoapListener(endpoint);
            eventSources.remove(this);
        }

        public void sentEvent(String event, String data) {
            try {
                emitter.event(event, data);
            } catch (IOException e) {
                e.printStackTrace();
                onClose();
            }
        }

        public String getEndpoint() {
            return endpoint;
        }
    }
}
