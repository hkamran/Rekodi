package com.hkamran.mocking.web.gui;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.DefaultHandler;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.server.nio.SelectChannelConnector;


public class Jetty {

	public static void main(String[] args) throws Exception {
	    Server server = new Server();
	    SelectChannelConnector connector = new SelectChannelConnector();
	    connector.setPort(8080);
	    server.addConnector(connector);

	    ResourceHandler resource_handler = new ResourceHandler();
	    resource_handler.setDirectoriesListed(true);
	    resource_handler.setWelcomeFiles(new String[]{ "index.html" });
	    
	    String dir = Jetty.class.getClassLoader().getResource("web").toExternalForm();
	    resource_handler.setResourceBase(dir);

	    HandlerList handlers = new HandlerList();
	    handlers.setHandlers(new Handler[] { resource_handler, new DefaultHandler() });
	    server.setHandler(handlers);

	    server.start();
	    server.join();
	}
	
}
