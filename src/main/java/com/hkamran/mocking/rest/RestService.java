package com.hkamran.mocking.rest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hkamran.mocking.Event;
import com.hkamran.mocking.FilterManager;
import com.hkamran.mocking.Request;


@Path("/rest")
public class RestService {

	private static FilterManager filter;

	public static void setFilter(FilterManager filter) {
		RestService.filter = filter;
	}

	@GET
	@Path("/events")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getEvents() {
		
		List<Event> events = filter.getEvents();
		JSONArray results = new JSONArray();
		for (Event event : events) {
			JSONObject result = new JSONObject();
			
			JSONObject request = new JSONObject(event.request.getHeaders());
			request.put("hashCode", event.request.hashCode());
			request.put("content", event.request.getContent());
			
			JSONObject response = new JSONObject(event.response.getHeaders());
			response.put("hashCode", event.response.hashCode());
			response.put("content", event.response.getContent());
			
			
			result.put("request", request);
			result.put("response", response);

			result.put("start", event.start);
			result.put("duration", event.duration);
			result.put("state", event.isRecordered);
			
			results.put(result);
		}
		
		return Response.status(200).entity(results.toString(2))
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT")
				.allow("OPTIONS").build();
	}
	
	public static void start() {
		ResourceConfig config = new ResourceConfig();
		config.packages("com.hkamran.mocking.rest");
		ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(
				config));
		Server server = new Server(7090);
		ServletContextHandler context = new ServletContextHandler(server, "/");
		context.addServlet(jerseyServlet, "/*");
		try {
			server.start();

		} catch (Exception e) {

		}
	}

	public static void main(String[] args) throws Exception {


	}

}
