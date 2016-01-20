package com.hkamran.mocking;

import io.netty.util.internal.StringUtil;

import java.util.HashMap;
import java.util.Map;

public class Header {
	Map<String, String> headers = new HashMap<String, String>();
	
	public void add(String attribute, String val) {
		if (headers.containsKey(attribute)) {
			
		}
		headers.put(attribute, val);
	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder();
		Integer count = 0;
		for (String key : headers.keySet()) {
			if (count == headers.size() - 1) {
				buf.append(key + ": " + headers.get(key));
			} else {
				buf.append(key + ": " + headers.get(key) + StringUtil.NEWLINE);
			}
			count++;
		}
		
		return buf.toString();
	}
}
