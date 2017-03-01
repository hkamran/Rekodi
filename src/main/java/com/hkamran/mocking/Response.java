package com.hkamran.mocking;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.CharsetUtil;
import io.netty.util.internal.StringUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringEscapeUtils;
import org.json.JSONObject;

import com.hkamran.mocking.Filter.State;
import com.hkamran.mocking.util.Formatter;

/**
 * This class represents the HTTP response that the client has gotten.
 *  
 * @author Hooman Kamran
 */
public class Response {

	private Map<String, String> headers = new HashMap<String, String>();
	
	private String protocol;
	private Integer status;
	private String content;
	private State state;
	
	private Integer id = -1;
	private Integer parent = -1;
	
	public Response(FullHttpResponse res, State state) {
		FullHttpResponse resCopy = (FullHttpResponse) res.copy();
		resCopy.retain();
		
		this.content = parseContent(resCopy);
		this.protocol = resCopy.getProtocolVersion().toString();
		this.status = resCopy.getStatus().code();
		
		copyHeaders(resCopy);
		this.state = state;
		
	}

	private void copyHeaders(FullHttpResponse resCopy) {
		for (Entry<String, String> entry : resCopy.headers()) {
			String key = entry.getKey();
			String value = entry.getValue();
			
			if (key.equalsIgnoreCase("Content-Length")) {
				Integer length = content.length();
				value = length.toString();
			}
			
			if (value == null) {
				value = "";
			}
			
			headers.put(key, value);
		}
	}
	
	public Response(Map<String, String> headers, String content, String protocol, Integer status, State state) {
		this.headers = headers;
		this.content = content;
		this.protocol = protocol;
		this.status = status;
		this.state = state;
	}

	private String parseContent(FullHttpResponse res) {
		ByteBufInputStream bufInputStream = new ByteBufInputStream(res.content().copy());
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

	public Response clone() {
		return new Response(getHeaders(), getContent(), getProtocol(), getStatus(), getState());
	}
	
	public State getState() {
		return state;
	}
	
	public String getContent() {
		return content;
	}

	public Integer getStatus() {
		return this.status;
	}

	public String getProtocol() {
		return this.protocol;
	}
	
	public void setId(Integer id) {
		this.id = id;
	}
	
	public int getId() {
		return this.id;
	}

	public void setContent(String content) {
		this.content = content;
	}
	
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	public void setProtocol(HttpVersion version) {
		this.protocol = version.toString();
	}
	
	public void setState(State state) {
		this.state = state;
	}
	
	public void setProtocol(String version) {
		this.protocol = version;
	}
	
	public void setParent(Integer hashCode) {
		this.parent = hashCode;
	}
	
	public Integer getParent() {
		return parent;
	}
	
	public Map<String, String> getHeaders() {
		return headers;
	}
	
//	@Override
//	public int hashCode() {
//		final int prime = 31;
//		int result = 1;
//		String content = toString();
//		int charValue = 0;
//		for (char c : content.toCharArray()) {
//			charValue += c;
//		}
//		result = prime * result + charValue;
//		return result;
//	}
//
//	@Override
//	public boolean equals(Object obj) {
//		if (this == obj)
//			return true;
//		if (obj == null)
//			return false;
//		if (getClass() != obj.getClass())
//			return false;
//		Response other = (Response) obj;
//		if (res == null) {
//			if (other.res != null)
//				return false;
//		} else if (!toString().equalsIgnoreCase(other.toString())) {
//			return false;
//		}
//		return true;
//	}
	
	public String toString() {
		StringBuilder buf = new StringBuilder();

		buf.append("Response" + StringUtil.NEWLINE);
		buf.append("   Status: " + getStatus().toString() + StringUtil.NEWLINE);
		buf.append("   Protocol: " + getProtocol() + StringUtil.NEWLINE);
		buf.append("   Headers: " + headers.toString() + StringUtil.NEWLINE);
		buf.append("   Content: " + StringUtil.NEWLINE);
		
		buf.append(StringEscapeUtils.unescapeJava(getContent()) + StringUtil.NEWLINE);
		
		return buf.toString();
	}
	
	public JSONObject toJSON() {
		JSONObject responseJSON = new JSONObject();
		responseJSON.put("status", getStatus());
		responseJSON.put("protocol", getProtocol());
		responseJSON.put("content", getContent());
		responseJSON.put("headers", getHeaders());
		responseJSON.put("hashCode", hashCode());
		responseJSON.put("id", id);
		responseJSON.put("state", getState());
		responseJSON.put("parent", parent);

		return responseJSON;
	}
	
	
	public static Response parseJSON(String source) {
		JSONObject json = new JSONObject(source);
		

		Integer status = json.getInt("status");
		String protocol = json.getString("protocol");
		String content = json.getString("content");
		Integer id = json.getInt("id");
		State state = State.valueOf(json.getString("state"));
		Integer parent = json.getInt("parent");
		
		JSONObject headersJSON = json.getJSONObject("headers");
		
		Map<String, String> headers = new HashMap<String, String>();
		for (Object key : headersJSON.keySet()) {
			headers.put(key.toString(), headersJSON.getString(key.toString()));
		}
		
		Response response = new Response(headers, content, protocol, status, state);
		response.setParent(parent);
		response.setId(id);
		
		
		return response;
	}
	
	public HttpResponse getHTTPObject() {
		HttpVersion version = HttpVersion.valueOf(protocol);
		HttpResponseStatus status = HttpResponseStatus.valueOf(this.status);
		ByteBuf content = Unpooled.copiedBuffer(this.content, CharsetUtil.UTF_8);
		DefaultFullHttpResponse response = new DefaultFullHttpResponse(version, status, content);

		
		HttpHeaders headers = response.headers();

		if (!headers.contains(HttpHeaders.Names.CONNECTION)) {
			headers.set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.CLOSE);
		}
		
		for (String key : this.headers.keySet()) {
			headers.set(key, this.headers.get(key));
		}
		
		return response;
	}

}
