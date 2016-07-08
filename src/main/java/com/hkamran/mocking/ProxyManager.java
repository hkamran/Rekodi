package com.hkamran.mocking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class ProxyManager {
	private static Integer count = 0;
	Map<Integer, Proxy> proxies = new HashMap<Integer, Proxy>();
	
	public Integer add(String name, Integer port) {
		Integer id = count;
		Proxy proxy = new Proxy(id, name, port);
		proxies.put(id, proxy);
		
		count = count + 1;
		proxy.start();
		return id;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		for (Integer id : proxies.keySet()) {
			Proxy proxy = proxies.get(id);
			json.put(id.toString(), proxy.toJSON());
		}
		return json;
	}
	
	public void remove(Integer id) {
		proxies.remove(id);
	}
	
	public Proxy get(Integer id) {
		return proxies.get(id);
	}
	
	public List<Proxy> getProxies() {
		List<Proxy> proxies = new ArrayList<Proxy>();
		for (Integer key : this.proxies.keySet()) {
			Proxy proxy = this.proxies.get(key);
			proxies.add(proxy);
		}
		return proxies;
	}
}
