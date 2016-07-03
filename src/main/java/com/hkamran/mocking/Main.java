package com.hkamran.mocking;

import javax.servlet.ServletException;

import org.apache.log4j.Logger;

import com.hkamran.mocking.FilterManager.State;
import com.hkamran.mocking.servers.HTTPServer;
import com.hkamran.mocking.servers.ProxyServer;
import com.hkamran.mocking.websockets.EventSocket;

/**
 * Hello world!
 *
 */
public class Main {

	private final static Logger log = Logger.getLogger(Main.class);

	public static void main(String[] args) throws Exception {
		
		/**
		 * Setup Filter
		 */
		
		log.info("Starting up Recorder....");
		
		FilterManager filter = new FilterManager();
		Integer port = 80;
		String host = "www.thomas-bayer.com";
		
		filter.setState(State.PROXY);
		filter.setRedirectInfo(host, port);
		filter.setRedirectState(true);
		
		
		HTTPServer frontEnd = new HTTPServer();
		frontEnd.addFilter("default", filter);
		ProxyServer defaultProxy = new ProxyServer(filter);

		
		/**
		 * Start Program
		 */
		EventSocket.setFilter(filter);
		defaultProxy.start(9090);
		frontEnd.start(8090);
	}
}
