package com.hkamran.mocking.servers;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hkamran.mocking.Event;
import com.hkamran.mocking.FilterManager;
import com.hkamran.mocking.Request;
import com.hkamran.mocking.Tape;

@Path("/")
public class HTTPServer {

	private final static Logger log = Logger.getLogger(HTTPServer.class);
	private static Map<String, FilterManager> filters = new HashMap<String, FilterManager>();

	public void addFilter(String name, FilterManager filter) {
		filters.put(name, filter);
	}
	
	public Map<String, FilterManager> getFilters() {
		return filters;
	}
	
	/*
	   _____ ______ _______ _______ ______ _____   _____ 
	  / ____|  ____|__   __|__   __|  ____|  __ \ / ____|
	 | |  __| |__     | |     | |  | |__  | |__) | (___  
	 | | |_ |  __|    | |     | |  |  __| |  _  / \___ \ 
	 | |__| | |____   | |     | |  | |____| | \ \ ____) |
	  \_____|______|  |_|     |_|  |______|_|  \_\_____/ 
	                                                                                                   	 
	 */
	
	/**
	 * Get the events
	 */
	@GET
	@Path("/{proxyName}/events")
	@Produces({MediaType.APPLICATION_JSON})
	public Response getEvents(@PathParam("proxyName") String proxyName) {
		
		//log.info("Requesting events for " + proxyName);
		FilterManager filter = filters.get(proxyName);
		List<Event> events = filter.getEvents();
		
		
		JSONArray eventsJSON = new JSONArray();
		for (Event event : events) {
			eventsJSON.put(event.toJSON());
		}
		
		return Response.status(200).entity(eventsJSON.toString(2))
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.allow("OPTIONS").build();		
	}
	
	/**
	 * Get the Request
	 */
	@GET
	@Path("/{proxyName}/tape/{requestID}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRequest(@PathParam("proxyName") String proxyName, @PathParam("requestID") String requestID) {
		log.info("Requesting request information for " + proxyName + " on request " + requestID);
		
		FilterManager filter = filters.get(proxyName);
		Tape tape = filter.getTape();
		Request request = tape.getRequest(requestID);
		
		if (request == null) {
			return Response.status(400).entity("")
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.allow("OPTIONS").build();
		}

		return Response.status(200).entity(request.toJSON().toString(2))
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.allow("OPTIONS").build();
	}
	
	/**
	 * Get the Response
	 */
	@GET
	@Path("/{proxyName}/tape/{requestID}/{responseID}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResponse(@PathParam("proxyName") String proxyName, 
			@PathParam("requestID") String requestID, 
			@PathParam("responseID") Integer responseID) {
		log.info("Requesting response information for " + proxyName + " on request " + requestID + " on response " + responseID);
		
		FilterManager filter = filters.get(proxyName);
		Tape tape = filter.getTape();
		Request request = tape.getRequest(requestID);
		if (request == null) {
			//error
		}
		
		List<com.hkamran.mocking.Response> responses = tape.getResponses(request);
		if (responseID < 0 || responseID >= responses.size()) {
			//error
		}
		
		com.hkamran.mocking.Response response = responses.get(responseID);
		JSONObject responseJSON = response.toJSON();
		
		return Response.status(200).entity(responseJSON.toString(2))
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.allow("OPTIONS").build();
	}

	/**
	 * Get the Tape 
	 */	
	@GET
	@Path("/{proxyName}/tape")
	@Produces({MediaType.APPLICATION_JSON})
	public Response getTape(@PathParam("proxyName") String proxyName) {
		log.info("Requesting tape information for " + proxyName);
		
		FilterManager filter = filters.get(proxyName);
		Tape tape = filter.getTape();
		JSONObject tapeJSON = tape.toJSON();
		
		return Response.status(200).entity(tapeJSON.toString(2))
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.allow("OPTIONS").build();		
	}
	
	/**
	 * Get Filter Settings
	 */
	@GET
	@Path("/{proxyName}")
	@Produces({MediaType.APPLICATION_JSON})
	public Response getSettings(@PathParam("proxyName") String proxyName) {
		log.info("Requesting filter information for " + proxyName);
		
		FilterManager filter = filters.get(proxyName);
		JSONObject filterSettings = filter.toJSON();
		
		return Response.status(200).entity(filterSettings.toString(2))
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.allow("OPTIONS").build();		
	}
	
	/*
	   _____ ______ _______ _______ ______ _____   _____ 
	  / ____|  ____|__   __|__   __|  ____|  __ \ / ____|
	 | (___ | |__     | |     | |  | |__  | |__) | (___  
	  \___ \|  __|    | |     | |  |  __| |  _  / \___ \ 
	  ____) | |____   | |     | |  | |____| | \ \ ____) |
	 |_____/|______|  |_|     |_|  |______|_|  \_\_____/ 
	                                                    
	 */
	
	@POST
	@Path("/{proxyName}/settings")
	@Consumes({MediaType.APPLICATION_JSON})
	public Response setSettings(@PathParam("proxyName") String proxyName,  final String json) {
		log.info("Updating settings " + proxyName);
		
		try {
			FilterManager filter = filters.get(proxyName);
			filter.parseJSON(json);
		} catch (Exception e) {
			log.error("Updating settings " + proxyName + " failed.");
			e.printStackTrace();
		}
		return Response.status(200).entity("")
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.header("Access-Control-Allow-Headers", "Content-Type, Content-Range, Content-Disposition, Content-Description")
				.allow("OPTIONS", "POST").build();	
	}
	
	@OPTIONS
	@Path("/{proxyName}/settings")
	public Response getSettingsOptions(@PathParam("proxyName") String proxyName) {
		return Response.status(200).entity("")
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.header("Access-Control-Allow-Headers", "Content-Type, Content-Range, Content-Disposition, Content-Description")
				.allow("OPTIONS", "POST").build();	
	}

	
	@POST
	@Path("/{proxyName}/tape/{requestID}")
	@Consumes({MediaType.APPLICATION_JSON})
	public Response setRequest(@PathParam("proxyName") String proxyName,  
							   @PathParam("requestID") String requestID, 
							   final String json) {
		try {
			Request request = Request.parseJSON(json);		
			FilterManager filter = filters.get(proxyName);
			filter.getTape().setRequest(requestID, request);
			log.info("Updating request " + requestID + " on " + proxyName);
			
			return Response.status(200).entity(request.toJSON().toString(2))
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.allow("OPTIONS").build();				
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Response.status(400).entity("")
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.allow("OPTIONS").build();					
	}
	
	@OPTIONS
	@Path("/{proxyName}/tape/{requestID}")
	public Response getRequestOptions(@PathParam("proxyName") String proxyName) {
		return Response.status(200).entity("")
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.header("Access-Control-Allow-Headers", "Content-Type, Content-Range, Content-Disposition, Content-Description")
				.allow("OPTIONS", "POST").build();	
	}
	
	@POST
	@Path("/{proxyName}/tape/{requestID}/{responseID}")
	@Consumes({MediaType.APPLICATION_JSON})
	public Response setResponse(@PathParam("proxyName") String proxyName, 
								@PathParam("requestID") String requestID,  
								@PathParam("responseID") Integer responseID, 
								final String json) {
		
		try {
			com.hkamran.mocking.Response response = com.hkamran.mocking.Response.parseJSON(json);		
			FilterManager filter = filters.get(proxyName);
			Request request = filter.getTape().getRequest(requestID);
			List<com.hkamran.mocking.Response> responses = filter.getTape().getResponses(request);
			
			responses.set(responseID, response);
			log.info("Updating response " + responseID + " for " + requestID + " on " + proxyName);
			
			return Response.status(200).entity(response.toJSON().toString(2))
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.allow("OPTIONS").build();		
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return Response.status(200).entity("")
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.allow("OPTIONS").build();	
	}
	
	@OPTIONS
	@Path("/{proxyName}/tape/{requestID}/{responseID}")
	public Response getResponseOptions(@PathParam("proxyName") String proxyName) {
		return Response.status(200).entity("")
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.header("Access-Control-Allow-Headers", "Content-Type, Content-Range, Content-Disposition, Content-Description")
				.allow("OPTIONS", "POST").build();	
	}
	
	@POST
	@Path("/{proxyName}/tape")
	@Consumes({MediaType.APPLICATION_JSON})
	public Response setTape(@PathParam("proxyName") String proxyName,  
							final String json) {
		try {
			FilterManager filter = filters.get(proxyName);
			filter.setTape(Tape.parseJSON(json));

			return Response.status(200).entity(filter.getTape().toJSON().toString(2))
					.header("Access-Control-Allow-Origin", "*")
					.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
					.allow("OPTIONS").build();		
		} catch (Exception e) {
			e.printStackTrace();
		}

		return Response.status(400).entity("")
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.allow("OPTIONS").build();			
	}
	
	@OPTIONS
	@Path("/{proxyName}/tape")
	public Response getTapeOptions(@PathParam("proxyName") String proxyName) {
		return Response.status(200).entity("")
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.header("Access-Control-Allow-Headers", "Content-Type, Content-Range, Content-Disposition, Content-Description")
				.allow("OPTIONS", "POST").build();	
	}
	
	
	public void start(Integer port) {
		log.info("Starting HTTP Server at " + port);
		
		//Set Jersey Classes
		ResourceConfig config = new ResourceConfig();
		Set<Class<?>> s = new HashSet<Class<?>>();
        s.add(HTTPServer.class);
		config.registerClasses(s);
		
		ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(config));
		
		//Create Jersey Handler (function is to handle REST)
		Server server = new Server(port);
		ServletContextHandler restHandler = new ServletContextHandler(server, "/rest");
		restHandler.addServlet(jerseyServlet, "/*");

		//Create Static Handler (Function is to serve HTML/JS/CSS
	    ResourceHandler staticHandler = new ResourceHandler();
	    staticHandler.setWelcomeFiles(new String[]{ "index.html" });
	    String dir = HTTPServer.class.getClassLoader().getResource("web").toExternalForm();
	    staticHandler.setResourceBase(dir);

	    HandlerList handlers = new HandlerList();
	    handlers.setHandlers(new Handler[] { restHandler, staticHandler });
				
	    server.setHandler(handlers);

		try {
            server.start();
            server.join();
        } catch (Exception e) {
			e.printStackTrace();
		} finally {
        	server.destroy();
        }
	}
	
  
}
