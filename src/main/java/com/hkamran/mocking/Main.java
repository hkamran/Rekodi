package com.hkamran.mocking;

import org.apache.log4j.Logger;

import com.hkamran.mocking.Filter.State;
import com.hkamran.mocking.servers.HTTPServer;
import com.hkamran.mocking.servers.WebSocket;

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
		
//		Filter filter = new Filter();

//		
//		
		HTTPServer frontEnd = new HTTPServer();
//	frontEnd.addFilter("default", filter);
		
		Proxies proxies = new Proxies();
		Integer id = proxies.add("Default Service", 9090);
		Proxy proxy = proxies.get(id);
		Filter filter = proxy.getFilter();
		
		Integer port = 80;
		String host = "www.thomas-bayer.com";
		
		filter.setState(State.PROXY);
		filter.setRedirectInfo(host, port);
		filter.setRedirectState(true);
		
		/**
		 * Start Program
		 */
		WebSocket.setProxyManager(proxies);
		frontEnd.start(8090);
	}
}
