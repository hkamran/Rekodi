package com.hkamran.mocking;

import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.internal.StringUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.http.entity.ContentType;
import org.json.JSONObject;

import com.hkamran.mocking.util.Formatter;

public class Response {
	private static final String CONTENT_LENGTH = "Content-Length";
	private static final String CONTENT_TYPE = "Content-Type";
	
	private DefaultFullHttpResponse res;

	public Response(DefaultFullHttpResponse res) {
		this.res = (DefaultFullHttpResponse) res.copy();
		this.res.retain();
	}

	public String getContent() {
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

	public HttpObject getHttpObject() {
		return res.duplicate();
	}

	public Boolean isXML() {
		String type = res.headers().get(CONTENT_TYPE);
		if (type == null) {
			type = "";
		}
		return type.contains("text/xml");
	}
	
	public Boolean isText() {
		String type = res.headers().get(CONTENT_TYPE);
		if (type == null) {
			type = "";
		}
		return type.contains("text/html");
	}

	public Boolean isJSON() {
		String type = res.headers().get(CONTENT_TYPE);
		if (type == null) {
			type = "";
		}
		return type.contains("application/json");
	}

	public Boolean isHTML() {
		String type = res.headers().get(CONTENT_TYPE);
		if (type == null) {
			type = "";
		}
		return type.contains("text/html");
	}

	public Integer getStatus() {
		return res.getStatus().code();
	}

	public String getProtocol() {
		return res.getProtocolVersion().toString();
	}

	public String getContentType() {
		String contentType = res.headers().get(CONTENT_TYPE);
		if (contentType == null) {
			return "";
		}
		return contentType;
	}

	public void setContent(String value, ContentType type) {
		res.content().clear();
		res.content().writeBytes(value.getBytes(StandardCharsets.UTF_8));
		res.headers().set("Content-Length", value.length());
		setContentType(type);
	}

	public void setContentType(ContentType type) {
		res.headers().set(CONTENT_TYPE, type.toString());
	}
	
	public void setStatus(HttpResponseStatus status) {
		res.setStatus(status);
	}
	
	public void setHttpVersion(HttpVersion version) {
		res.setProtocolVersion(version);
	}
	
	public Map<String, String> getHeaders() {
		Map<String, String> headers = new HashMap<String, String>();
		headers.put("Id", new Integer(this.hashCode()).toString());
		headers.put("Status", res.getStatus().toString());
		headers.put("Protocol", res.getProtocolVersion().text());
		headers.put("Content-Type", res.headers().get(CONTENT_TYPE));
		headers.put("Content-Length", res.headers().get(CONTENT_LENGTH));
		
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
		buf.append("   Status: " + res.getStatus().toString() + StringUtil.NEWLINE);
		buf.append("   Protocol: " + res.getProtocolVersion().text() + StringUtil.NEWLINE);
		buf.append("   Content-Length: " + res.headers().get(CONTENT_LENGTH) + StringUtil.NEWLINE);
		buf.append("   Content-Type: " + res.headers().get(CONTENT_TYPE) + StringUtil.NEWLINE);
		buf.append("   Content: " + StringUtil.NEWLINE);
		
		if (isJSON() || isXML() || isText()) {
			buf.append(StringEscapeUtils.unescapeJava(getContent()) + StringUtil.NEWLINE);
		} else {
			buf.append("Cannot be displayed " + StringUtil.NEWLINE);
		}
		return buf.toString();
	}
	
	public String toJSON() {
		JSONObject responseJSON = new JSONObject();
		responseJSON.put("status", getStatus());
		responseJSON.put("type", getContentType());
		responseJSON.put("protocol", getProtocol());
		responseJSON.put("content", getContent());
		return responseJSON.toString(2);
	}
	
	public static Response parseJSON(String json) {
		JSONObject responseJSON = new JSONObject(json);
		

		HttpResponseStatus status = HttpResponseStatus.valueOf(responseJSON.getInt("status"));
		HttpVersion version = HttpVersion.valueOf(responseJSON.getString("protocol"));
		ContentType type = ContentType.parse(responseJSON.getString("type"));
		String content = responseJSON.getString("content");
		
		DefaultFullHttpResponse defaultFull = new DefaultFullHttpResponse(version, status);
		Response response = new Response(defaultFull);
		response.setContent(content, type);
		
		return response;
	}

}
