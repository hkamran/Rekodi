package com.hkamran.mocking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONObject;

public class Proxies {
	private static Integer count = 0;
	Map<Integer, Proxy> proxies = new HashMap<Integer, Proxy>();
	
	public Integer add(String name, Integer port) {
		try {
			Integer id = count;
			Proxy proxy = new Proxy(id, name, port);
			proxy.start();
			proxies.put(id, proxy);
			count = count + 1;
			return id;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return -1;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		for (Integer key : this.proxies.keySet()) {
			Proxy proxy = this.proxies.get(key);
			json.put(key.toString(), proxy.toJSON());
		}
		return json;
	}
	
	public void remove(Integer id) {
		Proxy proxy = this.get(id);
		if (proxy == null) {
			return;
		}
		try {
			proxy.stop();
			proxies.remove(id);
		} catch (Exception e) {
			e.printStackTrace();
		}
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