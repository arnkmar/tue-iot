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
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.node.LwM2mObjectInstance;
import org.eclipse.leshan.core.node.LwM2mSingleResource;
import org.eclipse.leshan.core.node.codec.CodecException;
import org.eclipse.leshan.core.request.ContentFormat;
import org.eclipse.leshan.core.request.CreateRequest;
import org.eclipse.leshan.core.request.DeleteRequest;
import org.eclipse.leshan.core.request.DiscoverRequest;
import org.eclipse.leshan.core.request.ExecuteRequest;
import org.eclipse.leshan.core.request.ObserveRequest;
import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.request.WriteRequest;
import org.eclipse.leshan.core.request.WriteRequest.Mode;
import org.eclipse.leshan.core.request.exception.ClientSleepingException;
import org.eclipse.leshan.core.request.exception.InvalidRequestException;
import org.eclipse.leshan.core.request.exception.InvalidResponseException;
import org.eclipse.leshan.core.request.exception.RequestCanceledException;
import org.eclipse.leshan.core.request.exception.RequestRejectedException;
import org.eclipse.leshan.core.response.CreateResponse;
import org.eclipse.leshan.core.response.DeleteResponse;
import org.eclipse.leshan.core.response.DiscoverResponse;
import org.eclipse.leshan.core.response.ExecuteResponse;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.response.ObserveResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.core.response.WriteResponse;
import org.eclipse.leshan.server.LwM2mServer;
import org.eclipse.leshan.server.demo.servlet.json.LwM2mNodeDeserializer;
import org.eclipse.leshan.server.demo.servlet.json.LwM2mNodeSerializer;
import org.eclipse.leshan.server.demo.servlet.json.RegistrationSerializer;
import org.eclipse.leshan.server.demo.servlet.json.ResponseSerializer;
import org.eclipse.leshan.server.registration.Registration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import com.sun.tools.javac.util.List;

import org.eclipse.leshan.server.demo.LeshanServerSQLite;
import org.eclipse.leshan.server.demo.manager;

/**
 * Service HTTP REST API calls.
 */
public class ClientServlet extends HttpServlet {

    private static final String FORMAT_PARAM = "format";

    private static final Logger LOG = LoggerFactory.getLogger(ClientServlet.class);

    private static final long TIMEOUT = 5000; // ms

    private static final long serialVersionUID = 1L;

    private final LwM2mServer server;
    public static LwM2mServer server_static;
    
    public String Vehicle_req_ID; // to intercept request to PI for validation
    

    private final Gson gson;

    public ClientServlet(LwM2mServer server) {
        this.server = server;

        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeHierarchyAdapter(Registration.class,
                new RegistrationSerializer(server.getPresenceService()));
        gsonBuilder.registerTypeHierarchyAdapter(LwM2mResponse.class, new ResponseSerializer());
        gsonBuilder.registerTypeHierarchyAdapter(LwM2mNode.class, new LwM2mNodeSerializer());
        gsonBuilder.registerTypeHierarchyAdapter(LwM2mNode.class, new LwM2mNodeDeserializer());
        gsonBuilder.setDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
        this.gson = gsonBuilder.create();
        
        
        
        
        
        
        
        
        
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        // all registered clients
        if (req.getPathInfo() == null) {
            Collection<Registration> registrations = new ArrayList<>();
            for (Iterator<Registration> iterator = server.getRegistrationService().getAllRegistrations(); iterator
                    .hasNext();) {
                registrations.add(iterator.next());
            }

            String json = this.gson.toJson(registrations.toArray(new Registration[] {}));
            resp.setContentType("application/json");
            resp.getOutputStream().write(json.getBytes(StandardCharsets.UTF_8));
            resp.setStatus(HttpServletResponse.SC_OK);
            return;
        }

        String[] path = StringUtils.split(req.getPathInfo(), '/');
        if (path.length < 1) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
            return;
        }
        String clientEndpoint = path[0];

        // /endPoint : get client
        if (path.length == 1) {
            Registration registration = server.getRegistrationService().getByEndpoint(clientEndpoint);
            if (registration != null) {
                resp.setContentType("application/json");
                resp.getOutputStream().write(this.gson.toJson(registration).getBytes(StandardCharsets.UTF_8));
                resp.setStatus(HttpServletResponse.SC_OK);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().format("no registered client with id '%s'", clientEndpoint).flush();
            }
            return;
        }

        // /clients/endPoint/LWRequest/discover : do LightWeight M2M discover request on a given client.
        if (path.length >= 3 && "discover".equals(path[path.length - 1])) {
            String target = StringUtils.substringBetween(req.getPathInfo(), clientEndpoint, "/discover");
            try {
                Registration registration = server.getRegistrationService().getByEndpoint(clientEndpoint);
                if (registration != null) {
                    // create & process request
                    DiscoverRequest request = new DiscoverRequest(target);
                    DiscoverResponse cResponse = server.send(registration, request, TIMEOUT);
                    processDeviceResponse(req, resp, cResponse);
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().format("No registered client with id '%s'", clientEndpoint).flush();
                }
            } catch (RuntimeException | InterruptedException e) {
                handleException(e, resp);
            }
            return;
        }

        // /clients/endPoint/LWRequest : do LightWeight M2M read request on a given client.
        try {
            String target = StringUtils.removeStart(req.getPathInfo(), "/" + clientEndpoint);
            Registration registration = server.getRegistrationService().getByEndpoint(clientEndpoint);
            if (registration != null) {
                // get content format
                String contentFormatParam = req.getParameter(FORMAT_PARAM);
                ContentFormat contentFormat = contentFormatParam != null
                        ? ContentFormat.fromName(contentFormatParam.toUpperCase())
                        : null;

                // create & process request
                ReadRequest request = new ReadRequest(contentFormat, target);
                ReadResponse cResponse = server.send(registration, request, TIMEOUT);
                processDeviceResponse(req, resp, cResponse);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().format("No registered client with id '%s'", clientEndpoint).flush();
            }
        } catch (RuntimeException | InterruptedException e) {
            handleException(e, resp);
        }
    }
    
    public static String getResource(Registration registration, int code) throws SQLException {
    	
    	// get Occupancy and CarID 
    	// If called with registration Code =1 , SQL is updated 
    	
    	
    	//Code = 1 : occupancy return
    	// code = 2 : carID return 
    	
    	String target1 = "/32700/0/32801";
    	String target2 = "/32700/0/32802";
    	String target3 = "/32700/0/32803";
    	long TIMEOUT = 5000; // ms
    	try {
    		
    		
    	if(code ==3) {
    		
        	ReadRequest request3 = new ReadRequest(ContentFormat.fromName("JSON"), target3);
        	ReadResponse cResponse3 = server_static.send(registration, request3, TIMEOUT);
        	String[] path3 = StringUtils.split(cResponse3.getContent().toString(), ',');
        	String[] rates = StringUtils.split(path3[1], '=');
        	return rates[1];
    		
    	}
    		
    	ReadRequest request = new ReadRequest(ContentFormat.fromName("JSON"), target1);
    	ReadResponse cResponse = server_static.send(registration, request, TIMEOUT);
    	String[] path = StringUtils.split(cResponse.getContent().toString(), ',');
    	String[] occupancy = StringUtils.split(path[1], '=');

    	
    	
    	String carID="" ;
    	if(!occupancy[1].toLowerCase().equals("free")) {
    		
	    	ReadRequest request2 = new ReadRequest(ContentFormat.fromName("JSON"), target2);
	    	ReadResponse cResponse2 = server_static.send(registration, request2, TIMEOUT);
	    	
	    	String[] path2 = StringUtils.split(cResponse2.getContent().toString(), ',');
	    	String[] carIDs = StringUtils.split(path2[1], '=');
	    	carID = carIDs[1];
	    	
	    	if(code==2)
				return carID;

    	}

        LeshanServerSQLite.ToSQLDB("OVERVIEW",10,Instant.now().getEpochSecond(),"Active",registration.getEndpoint(),occupancy[1].toLowerCase(),carID,0,null,null);
        
        return occupancy[1];
    	}
    	catch (RuntimeException | InterruptedException e) {
    		System.out.println(e);
    		return null;
    	}
    }
    
    public static void startObservation(Registration registration, String occupancy) {
    	
        
        String clientEndpoint = null;
        // /clients/endPoint/LWRequest/observe : do LightWeight M2M observe request on a given client.
        if(occupancy!=null)
            try {
                String target = "/32700/0/32801";
                
                if (registration != null) {
                    // get content format                
                    ContentFormat contentFormat = ContentFormat.fromName("JSON");
                    // create & process request
                    ObserveRequest request = new ObserveRequest(contentFormat, target);                    
                    ObserveResponse cResponse = server_static.send(registration, request, TIMEOUT);
                } 
                else {
                	System.out.println("No registered client with id ");
                }
            } catch (RuntimeException | InterruptedException e) {
            	System.out.println("Exception ClientServlet:startObservation");
            }
            return;
    }    
    	


    private void handleException(Exception e, HttpServletResponse resp) throws IOException {
        if (e instanceof InvalidRequestException || e instanceof CodecException
                || e instanceof ClientSleepingException) {
            LOG.warn("Invalid request", e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            resp.getWriter().append("Invalid request:").append(e.getMessage()).flush();
        } else if (e instanceof RequestRejectedException) {
            LOG.warn("Request rejected", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().append("Request rejected:").append(e.getMessage()).flush();
        } else if (e instanceof RequestCanceledException) {
            LOG.warn("Request cancelled", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().append("Request cancelled:").append(e.getMessage()).flush();
        } else if (e instanceof InvalidResponseException) {
            LOG.warn("Invalid response", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().append("Invalid Response:").append(e.getMessage()).flush();
        } else if (e instanceof InterruptedException) {
            LOG.warn("Thread Interrupted", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().append("Thread Interrupted:").append(e.getMessage()).flush();
        } else {
            LOG.warn("Unexpected exception", e);
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().append("Unexpected exception:").append(e.getMessage()).flush();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] path = StringUtils.split(req.getPathInfo(), '/');
        
        
        if(path[0].equals("httpQuery")) { // Check for reservation slot in the requested time and return to the user        	        
		    // Check for Vehicle Registration		       		       	
	       	if(LeshanServerSQLite.validateVehicle("REGISTERED_VEHICLES",path[3]).equals("NoReg")) { // Check if the vehicle is registered
	       		processDeviceResponse_user(req, resp, "0;Please Register your vehicle first");
	       		return;
	       	}
        	int StartTime = Integer.valueOf(path[1]);
        	int Endtime = Integer.valueOf(path[2]);   
        	String rate= LeshanServerSQLite.userToDB(1, StartTime, Endtime);
        	rate = rate + ";"+LeshanServerSQLite.VehicleRecordCheck(path[3]);
        	
        	processDeviceResponse_user(req, resp, rate); 
        	
        	return;
        }
        

        if(path[0].equals("vehicleRegister")) { // Check for reservation slot in the requested time and return to the user        	        		       		       	
        	try {
	        		
	        	String VehicleID = path[1];
	        	String VehicleName = path[2];   
     	
			   String str = "INSERT INTO REGISTERED_VEHICLES (TIME,VEHID,VEHNAME,CRIMNL_RECD,DUES,COMMENTS) VALUES (" 
			   + Long.toString(Instant.now().getEpochSecond())
			   + ",'"+VehicleID
			   + "','"+VehicleName
			   + "','"+"NO"
			   + "','"+"0.0"
			   + "','"+""
			   + "');" ; 

				if(LeshanServerSQLite.insert(str))  	
					processDeviceResponse_user(req, resp, "Registration Success");
				else
					processDeviceResponse_user(req, resp, "Registration Failed");
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	return;
        }
        	
        if(path[0].equals("choice")) { // Add reservation data to the database
        	String StartTime = path[1];
        	String Endtime = path[2];       	
        	String ClietName = path[3];
        	String vehicleID = path[4];

        	Long now = Instant.now().getEpochSecond();
        	String rate= "Reserved" ;
        	
			   String str = "INSERT INTO ["+ ClietName  +"] (TIME,RSTART,REND,RCAR) VALUES (" 
			   + Long.toString(now)
			   + ",'"+StartTime
			   +"','"+ Endtime
			   + "','"+vehicleID
			   + "');" ; 
			   
			   
				   //Add reservation start to Scheduler
				   manager.addToHash(Integer.valueOf(StartTime), ClietName, vehicleID, "Start");
				   manager.addToHash(Integer.valueOf(Endtime), ClietName, null, "End");
				   
	
				   
			   
			   try {
				LeshanServerSQLite.insert(str);
				
				LeshanServerSQLite.ToSQLDB("IoTParking",1,Instant.now().getEpochSecond(),"Reservation",ClietName,StartTime,Endtime,0,null,vehicleID );
				   rate=rate+",ReferenceID,"+Long.toString(now); 
				processDeviceResponse_user(req, resp, rate);	   
				   
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	return;
        	
        }
        
        String clientEndpoint = path[0];

        // at least /endpoint/objectId/instanceId
        if (path.length < 3) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid path");
            return;
        }

        try {
            String target = StringUtils.removeStart(req.getPathInfo(), "/" + clientEndpoint);
            Registration registration = server.getRegistrationService().getByEndpoint(clientEndpoint);
            if (registration != null) {
                // get content format
                String contentFormatParam = req.getParameter(FORMAT_PARAM);
                ContentFormat contentFormat = contentFormatParam != null
                        ? ContentFormat.fromName(contentFormatParam.toUpperCase())
                        : null;
                        	
                // create & process request
                LwM2mNode node = extractLwM2mNode(target, req);
                
                WriteRequest request = new WriteRequest(Mode.REPLACE, contentFormat, target, node);
                WriteResponse cResponse = server.send(registration, request, TIMEOUT);
                
                processDeviceResponse(req, resp, cResponse);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().format("No registered client with id '%s'", clientEndpoint).flush();
            }
        } catch (RuntimeException | InterruptedException e) {
            handleException(e, resp);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] path = StringUtils.split(req.getPathInfo(), '/');
        String clientEndpoint = path[0];

        // /clients/endPoint/LWRequest/observe : do LightWeight M2M observe request on a given client.
        if (path.length >= 3 && "observe".equals(path[path.length - 1])) {
            try {
                String target = StringUtils.substringBetween(req.getPathInfo(), clientEndpoint, "/observe");
                Registration registration = server.getRegistrationService().getByEndpoint(clientEndpoint);
                if (registration != null) {
                    // get content format
                    String contentFormatParam = req.getParameter(FORMAT_PARAM);
                    ContentFormat contentFormat = contentFormatParam != null
                            ? ContentFormat.fromName(contentFormatParam.toUpperCase())
                            : null;

                    // create & process request
                    ObserveRequest request = new ObserveRequest(contentFormat, target);
                    ObserveResponse cResponse = server.send(registration, request, TIMEOUT);
                    processDeviceResponse(req, resp, cResponse);
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().format("no registered client with id '%s'", clientEndpoint).flush();
                }
            } catch (RuntimeException | InterruptedException e) {
                handleException(e, resp);
            }
            return;
        }

        String target = StringUtils.removeStart(req.getPathInfo(), "/" + clientEndpoint);

        // /clients/endPoint/LWRequest : do LightWeight M2M execute request on a given client.
        if (path.length == 4) {
            try {
                Registration registration = server.getRegistrationService().getByEndpoint(clientEndpoint);
                if (registration != null) {
                    ExecuteRequest request = new ExecuteRequest(target, IOUtils.toString(req.getInputStream()));
                    ExecuteResponse cResponse = server.send(registration, request, TIMEOUT);
                    processDeviceResponse(req, resp, cResponse);
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().format("no registered client with id '%s'", clientEndpoint).flush();
                }
            } catch (RuntimeException | InterruptedException e) {
                handleException(e, resp);
            }
            return;
        }

        // /clients/endPoint/LWRequest : do LightWeight M2M create request on a given client.
        if (2 <= path.length && path.length <= 3) {
            try {
                Registration registration = server.getRegistrationService().getByEndpoint(clientEndpoint);
                if (registration != null) {
                    // get content format
                    String contentFormatParam = req.getParameter(FORMAT_PARAM);
                    ContentFormat contentFormat = contentFormatParam != null
                            ? ContentFormat.fromName(contentFormatParam.toUpperCase())
                            : null;

                    // create & process request
                    LwM2mNode node = extractLwM2mNode(target, req);
                    if (node instanceof LwM2mObjectInstance) {
                        CreateRequest request = new CreateRequest(contentFormat, target, (LwM2mObjectInstance) node);
                        CreateResponse cResponse = server.send(registration, request, TIMEOUT);
                        processDeviceResponse(req, resp, cResponse);
                    } else {
                        throw new IllegalArgumentException("payload must contain an object instance");
                    }
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().format("no registered client with id '%s'", clientEndpoint).flush();
                }
            } catch (RuntimeException | InterruptedException e) {
                handleException(e, resp);
            }
            return;
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String[] path = StringUtils.split(req.getPathInfo(), '/');
        String clientEndpoint = path[0];

        // /clients/endPoint/LWRequest/observe : cancel observation for the given resource.
        if (path.length >= 3 && "observe".equals(path[path.length - 1])) {
            try {
                String target = StringUtils.substringsBetween(req.getPathInfo(), clientEndpoint, "/observe")[0];
                Registration registration = server.getRegistrationService().getByEndpoint(clientEndpoint);
                if (registration != null) {
                    server.getObservationService().cancelObservations(registration, target);
                    resp.setStatus(HttpServletResponse.SC_OK);
                } else {
                    resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    resp.getWriter().format("no registered client with id '%s'", clientEndpoint).flush();
                }
            } catch (RuntimeException e) {
                handleException(e, resp);
            }
            return;
        }

        // /clients/endPoint/LWRequest/ : delete instance
        try {
            String target = StringUtils.removeStart(req.getPathInfo(), "/" + clientEndpoint);
            Registration registration = server.getRegistrationService().getByEndpoint(clientEndpoint);
            if (registration != null) {
                DeleteRequest request = new DeleteRequest(target);
                DeleteResponse cResponse = server.send(registration, request, TIMEOUT);
                processDeviceResponse(req, resp, cResponse);
            } else {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                resp.getWriter().format("no registered client with id '%s'", clientEndpoint).flush();
            }
        } catch (RuntimeException | InterruptedException e) {
            handleException(e, resp);
        }
    }

    private void processDeviceResponse(HttpServletRequest req, HttpServletResponse resp, LwM2mResponse cResponse)
            throws IOException {
        if (cResponse == null) {
            LOG.warn(String.format("Request %s%s timed out.", req.getServletPath(), req.getPathInfo()));
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().append("Request timeout").flush();
        } else {
            String response = this.gson.toJson(cResponse);
            resp.setContentType("application/json");
            resp.getOutputStream().write(response.getBytes());
            resp.setStatus(HttpServletResponse.SC_OK);
        }
    }

    private void processDeviceResponse_user(HttpServletRequest req, HttpServletResponse resp, String response)
            throws IOException {
        if (response == null) {
            LOG.warn(String.format("Request %s%s timed out.", req.getServletPath(), req.getPathInfo()));
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
            resp.getWriter().append("Request timeout").flush();
        } else {
          
            resp.setContentType("application/json");
            resp.getOutputStream().write(response.getBytes());
            resp.setStatus(HttpServletResponse.SC_OK);
        }
    }    
    
    public void markParkingSpotReserved (String CarID, String ClientName) {

    	String target ="/32700/0/32801";
    	String content = "{\"id\":32801,\"value\":\"reserved\"}";   	
    	serverWriteToParkingSpot(target,content,ClientName);
    	
    	target ="/32700/0/32802";
    	content = "{\"id\":32802,\"value\":"+CarID+"}";   	
    	serverWriteToParkingSpot(target,content,ClientName);
    	
    }
    
    public void unmarkParkingSpotReserved (String ClientName) {
 	
    	String target ="/32700/0/32801";
    	String content = "{\"id\":32801,\"value\":\"free\"}";   	
    	serverWriteToParkingSpot(target,content,ClientName);
    	
    	
    }    
    
    private void serverWriteToParkingSpot(String target,  String content, String ClientName ) { //String Id, String Value, String target) {

        LwM2mNode node;
        try {
            node = gson.fromJson(content, LwM2mNode.class);

        } catch (JsonSyntaxException e) {
            throw new InvalidRequestException(e, "unable to parse json to tlv:%s", e.getMessage());
        }
        
        ContentFormat contentFormat = ContentFormat.fromName("JSON");
        
        WriteRequest request = new WriteRequest(Mode.REPLACE, contentFormat, target, node);
        Registration registration = server.getRegistrationService().getByEndpoint(ClientName);
        try {
			WriteResponse cResponse = server.send(registration, request, TIMEOUT);
		} catch (CodecException | InvalidResponseException | RequestCanceledException | RequestRejectedException
				| ClientSleepingException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
		}
        
    }
    

    
    private LwM2mNode extractLwM2mNode(String target, HttpServletRequest req) throws IOException {
        String contentType = StringUtils.substringBefore(req.getContentType(), ";");
        if ("application/json".equals(contentType)) {
            String content = IOUtils.toString(req.getInputStream(), req.getCharacterEncoding());
            Vehicle_req_ID = content; 
            LwM2mNode node;
            try {
                node = gson.fromJson(content, LwM2mNode.class);
            } catch (JsonSyntaxException e) {
                throw new InvalidRequestException(e, "unable to parse json to tlv:%s", e.getMessage());
            }
            return node;
        } else if ("text/plain".equals(contentType)) {
            String content = IOUtils.toString(req.getInputStream(), req.getCharacterEncoding());
            int rscId = Integer.valueOf(target.substring(target.lastIndexOf("/") + 1));
            return LwM2mSingleResource.newStringResource(rscId, content);
        }
        throw new InvalidRequestException("content type %s not supported", req.getContentType());
    }
}
