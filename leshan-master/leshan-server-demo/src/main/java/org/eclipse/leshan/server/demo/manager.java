package org.eclipse.leshan.server.demo;

import org.eclipse.leshan.core.node.LwM2mNode;
import org.eclipse.leshan.core.request.ReadRequest;
import org.eclipse.leshan.core.response.LwM2mResponse;
import org.eclipse.leshan.core.response.ReadResponse;
import org.eclipse.leshan.server.LwM2mServer;
import org.eclipse.leshan.server.demo.servlet.json.LwM2mNodeDeserializer;
import org.eclipse.leshan.server.demo.servlet.json.LwM2mNodeSerializer;
import org.eclipse.leshan.server.demo.servlet.json.RegistrationSerializer;
import org.eclipse.leshan.server.demo.servlet.json.ResponseSerializer;
import org.eclipse.leshan.server.registration.Registration;
import javax.servlet.http.HttpServlet;

import com.google.gson.GsonBuilder;

public class manager extends HttpServlet{
	
	public Registration newClient;
	String target = "/3303/0/5700";
	
	private static final long TIMEOUT = 5000; // ms
	private final LwM2mServer server;
	
    public manager(LwM2mServer server) {
       this. server = server;
    }
	
	
	public static void manager_init(Registration newClient) {
		newClient =newClient;
	}

	public void getname() {
		
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
