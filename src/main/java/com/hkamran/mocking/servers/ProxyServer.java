package com.hkamran.mocking.servers;

import org.apache.log4j.Logger;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.extras.SelfSignedMitmManager;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import com.hkamran.mocking.FilterManager;

public class ProxyServer {

	private final static Logger log = Logger.getLogger(ProxyServer.class);
	private FilterManager filter = null;
	private HttpProxyServer server;
	private Integer port;

	public ProxyServer(FilterManager filter) {
		this.filter = filter;
	}

	public void start(int port) {
		this.port = port;
		
		log.info("Starting Proxy Server " + filter.getHost() + " at " + this.port);

		server = DefaultHttpProxyServer.bootstrap().withPort(port)
				.withFiltersSource(filter)
				.withManInTheMiddle(new SelfSignedMitmManager()).start();
	}

	public void close() {
		try {
			server.stop();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
