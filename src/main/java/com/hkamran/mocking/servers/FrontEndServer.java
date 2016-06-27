package com.hkamran.mocking.servers;

import org.apache.log4j.Logger;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

import com.hkamran.mocking.FilterManager;

public class FrontEndServer {
	
	private final static Logger log = Logger.getLogger(FrontEndServer.class);
	
	public static void start(Integer port) {
	    Server server = new Server(port);
	    ResourceHandler resource_handler = new ResourceHandler();
	    resource_handler.setDirectoriesListed(true);
	    resource_handler.setWelcomeFiles(new String[]{ "index.html" });
	    
	    String dir = FrontEndServer.class.getClassLoader().getResource("web").toExternalForm();
	    resource_handler.setResourceBase(dir);

	    HandlerList handlers = new HandlerList();
	    handlers.setHandlers(new Handler[] { resource_handler, new DefaultHandler() });
	    server.setHandler(handlers);

	    try {
	    	log.info("Starting FontEnd at port " + port);
			server.start();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) throws Exception {
		FrontEndServer.start(8090);

	}
	
}
