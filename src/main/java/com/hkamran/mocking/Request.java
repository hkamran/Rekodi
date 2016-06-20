package com.hkamran.mocking;

import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.internal.StringUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONObject;

import com.hkamran.mocking.util.Formatter;

public class Request {

	public static enum MATCHTYPE {
		content, request;
	}

	private MATCHTYPE matchType = Request.MATCHTYPE.request;
	public String matchedString = "";

	private Map<String, String> headers = new HashMap<String, String>();
	
	private String content = "";
	private String protocol = "";
	private String method = "";
	private String uri = "";
	
	public Integer counter = 0;

	public Request(DefaultFullHttpRequest req) {
		DefaultFullHttpRequest reqCopy = (DefaultFullHttpRequest) req.copy();
		reqCopy.retain();
		
		for (Entry<String, String> entry : reqCopy.headers()) {
			String key = entry.getKey();
			String value = entry.getValue();
			if (value == null) {
				value = "";
			}
			
			headers.put(key, value);
		}
		
		content = parseContent(reqCopy);
		protocol = reqCopy.getProtocolVersion().toString();
		method = reqCopy.getMethod().toString();
		uri = reqCopy.getUri();
		
		
		System.out.println(toJSON());
	}
	
	public Request(Map<String, String> headers, String content, String protocol, String method, String uri) {
		this.content = content;
		this.protocol = protocol;
		this.method = method;
		this.uri = uri;
		this.headers = headers;
	}

	private String parseContent(DefaultFullHttpRequest req) {
		ByteBufInputStream bufInputStream = new ByteBufInputStream(req.content().copy());
		StringBuilder content = new StringBuilder();

		try {
			while (bufInputStream.available() > 0) {
				content.append((char) bufInputStream.readByte());
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				bufInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return Formatter.format(content.toString());
	}
	
	public String getContent() {
		return content;
	}

	public int compareTo(Request d) {
		return (this.uri).compareTo(d.getURI());
	}

	public Request clone() {
		return new Request(getHeaders(), getContent(), getProtocol(), getMethod(), getURI());
	}

	public String getMethod() {
		return method;
	}

	public String getProtocol() {
		return protocol;
	}

	public void setContent(String value) {
		this.content = value;
	}

	public MATCHTYPE getMatchType() {
		return matchType;
	}

	public void setMatchType(MATCHTYPE matchType) {
		this.matchType = matchType;
	}

	public String getURI() {
		return this.uri;
	}

	public void setURI(String uri) {
		this.uri = uri;
	}

	public void setMethod(HttpMethod method) {
		this.method = method.toString();
	}
	
	public void setMethod(String method) {
		this.method = method.toString();
	}

	public void setProtocol(HttpVersion httpVersion) {
		this.protocol = httpVersion.toString();
	}
	
	public void setProtocol(String httpVersion) {
		this.protocol = httpVersion.toString();
	}
	
	public void setHeader(String key, String value) {
		this.headers.put(key, value);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		String content = this.getURI() + this.getMethod() + this.getContent();
		int charValue = 0;
		for (char c : content.toCharArray()) {
			charValue += c;
		}
		result = prime * result + ((this.getURI() == null) ? 0 : charValue);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) 
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Request other = (Request) obj;
	
		if (!this.getURI().equalsIgnoreCase(other.getURI()) || !this.getMethod().equalsIgnoreCase(other.getMethod().toString())
				|| !getContent().equalsIgnoreCase(other.getContent()))
			return false;
		return true;
	}
	
	public Map<String, String> getHeaders() {	
		return headers;
	}
	
	public void setMatchString(String matchedString) {
		this.matchedString = matchedString;
	}

	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Request" + StringUtil.NEWLINE);
		buf.append("   Headers: " + headers.toString() + StringUtil.NEWLINE);
		buf.append("   URI: " + getURI() + StringUtil.NEWLINE);
		buf.append("   Protocol: " + getProtocol() + StringUtil.NEWLINE);
		buf.append("   Method: " + getMethod() + StringUtil.NEWLINE);
		buf.append("   Match Type: " + this.getMatchType().toString() + StringUtil.NEWLINE);
		buf.append("   Matched String: " + this.matchedString + StringUtil.NEWLINE);
		buf.append("   Content: " + StringUtil.NEWLINE);
		
		//if (isJSON() || isXML() || isText()) {
			buf.append(getContent() + StringUtil.NEWLINE);
		//} else {
		//	buf.append("Cannot be displayed " + StringUtil.NEWLINE);
		//}
		return buf.toString();
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("uri", getURI());
		json.put("protocol", this.getProtocol());
		json.put("method", this.getMethod());
		json.put("content", this.getContent());
		json.put("matchType", this.getMatchType());
		json.put("matchString", this.matchedString);
		json.put("id", this.hashCode());
		json.put("headers", this.headers);
		
		return json;
	}
	
	public static Request parseJSON(String source) {
		try {
			JSONObject json = new JSONObject(source);
			System.out.println(json);
			String uri = json.getString("uri");
			String protocol = json.getString("protocol");
			String method = json.getString("method");
			String content = json.getString("content");
			
			String matchType = json.getString("matchType");
			String matchString = json.getString("matchString");
			
			JSONObject headersJSON = json.getJSONObject("headers");
			
			Map<String, String> headers = new HashMap<String, String>();
			for (Object key : headersJSON.keySet()) {
				headers.put(key.toString(), headersJSON.getString(key.toString()));
			}
			
			Request request = new Request(headers, content, protocol, method, uri);
			request.setMatchType(MATCHTYPE.valueOf(matchType));
			request.setMatchString(matchString);
			
			return request;
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	
	public static void main(String[] args) {
		Request request = Request.parseJSON("{'headers':{'Accept-Language':'null'},'content':'','protocol':'HTTP/1.1','matchString':'','matchType':'request','method':'POST','uri':'/'}");
		System.out.println(request.toJSON());
	
	}

}
