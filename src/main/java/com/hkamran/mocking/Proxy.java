package com.hkamran.mocking;

import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.extras.SelfSignedMitmManager;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

public class Proxy {

	private final static Logger log = Logger.getLogger(Proxy.class);
	private Filter filter = null;
	private HttpProxyServer server;
	
	//Proxy Settings
	public Integer port;
	public Integer id;
	public String name;
	public Status status;
	
	public static enum Status {
		START, STOP
	}

	public Proxy(Integer id, String name, Integer port) {
		this.filter = new Filter();
		this.port = port;
		this.id = id;
		this.name = name;
		this.status = Status.STOP;
	}

	public void start() {
		log.info("Starting Proxy Server: " + id + " on " + filter.getHost() + " at " + this.port);

		server = DefaultHttpProxyServer.bootstrap().withPort(port)
				.withFiltersSource(filter)
				.withManInTheMiddle(new SelfSignedMitmManager()).start();
		
		this.status = Status.START;
		
	}
	
	public void stop() {
		try {
			server.stop();
			this.status = Status.STOP;
			log.info("Stopping Proxy Server: " + id + " on " + filter.getHost() + " at " + this.port);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public Filter getFilter() {
		return filter;
	}
	
	public Integer getID() {
		return id;
	}
	
	public static Proxy parseJSON(String source) {
		JSONObject json = new JSONObject(source);
		Integer id = json.getInt("id");
		Integer port = json.getInt("port");
		String name = json.getString("name");
		Status status = Status.valueOf(json.getString("status"));
		
		Proxy proxy = new Proxy(id, name, port);
		proxy.status = status;
		return proxy;
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("id", id);
		json.put("port", port);
		json.put("name", name);
		json.put("status", this.status);
		
		return json;
	}
}
