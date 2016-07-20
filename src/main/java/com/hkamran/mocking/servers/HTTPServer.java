package com.hkamran.mocking.servers;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.ServletException;
import javax.websocket.DeploymentException;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.DefaultServlet;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.websocket.jsr356.server.ServerContainer;
import org.eclipse.jetty.websocket.jsr356.server.deploy.WebSocketServerContainerInitializer;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.servlet.ServletContainer;
import org.json.JSONObject;

import com.hkamran.mocking.Filter;
import com.hkamran.mocking.Proxies;
import com.hkamran.mocking.Proxy;
import com.hkamran.mocking.Tape;

@Path("/")
public class HTTPServer {

	private final static Logger log = LogManager.getLogger(HTTPServer.class);
	private static Proxies proxies;
	
	public static void setProxyManager(Proxies proxies) {
		HTTPServer.proxies = proxies;
	}

	/**
	 * Get the Tape
	 */
	@GET
	@Path("/{proxyId}/tape")
	@Produces({ MediaType.APPLICATION_JSON })
	public Response getTape(@PathParam("proxyId") Integer proxyName) {
		log.info("Requesting tape information for " + proxyName);

		Proxy proxy = proxies.get(proxyName);
		Filter filter = proxy.getFilter();
		Tape tape = filter.getTape();
		JSONObject tapeJSON = tape.toJSON();

		return Response
				.status(200)
				.entity(tapeJSON.toString(2))
				.header("Access-Control-Allow-Origin", "*")
				.header("Access-Control-Allow-Methods",
						"GET, POST, DELETE, PUT")
				.header("Content-Disposition", "attachment; filename=tape.json")
				.allow("OPTIONS").build();
	}

	public void start(Integer port) throws ServletException, DeploymentException {
		log.info("Starting HTTP Server at " + port);
		
		//Set Jersey Classes
		ResourceConfig config = new ResourceConfig();
		Set<Class<?>> s = new HashSet<Class<?>>();
        s.add(HTTPServer.class);
        s.add(WebSocket.class);
		config.registerClasses(s);
		
		Server server = new Server(port);
		


		//Create Jersey Handler (function is to handle REST)
		ServletHolder jerseyServlet = new ServletHolder(new ServletContainer(config));
		ServletContextHandler restHandler = new ServletContextHandler(server, "/rest");
		restHandler.addServlet(jerseyServlet, "/*");

		//Create WS
		ServletHolder wsServlet = new ServletHolder(new DefaultServlet());
		ServletContextHandler wsHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
		wsHandler.setContextPath("/ws");
		wsHandler.addServlet(wsServlet, "/*");
		
		//Create Static Handler (Function is to serve HTML/JS/CSS
	    ResourceHandler staticHandler = new ResourceHandler();
	    staticHandler.setWelcomeFiles(new String[]{ "index.html" });
	    String dir = HTTPServer.class.getClassLoader().getResource("web").toExternalForm();
	    staticHandler.setResourceBase(dir);

	    HandlerList handlers = new HandlerList();
	    handlers.setHandlers(new Handler[] {
	    		wsHandler, staticHandler, restHandler
	    });
				
	    server.setHandler(handlers);
	    
        ServerContainer container = WebSocketServerContainerInitializer.configureContext(wsHandler); 
        container.addEndpoint(WebSocket.class); 
        
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
