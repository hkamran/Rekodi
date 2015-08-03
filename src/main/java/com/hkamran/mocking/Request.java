package com.hkamran.mocking;

import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.util.internal.StringUtil;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.apache.http.entity.ContentType;

public class Request {

	public static enum MATCHTYPE {
		content, request;
	}

	private MATCHTYPE matchType = Request.MATCHTYPE.request;
	public String matchedString = "";

	private static final String CONTENT_TYPE = "Content-Type";

	private DefaultFullHttpRequest req;
	public Integer counter = 0;

	public Request(DefaultFullHttpRequest req) {
		this.req = (DefaultFullHttpRequest) req.copy();
		this.req.retain();
	}

	public String getContent() {
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
		return content.toString();
	}

	public int compareTo(Request d) {
		return (this.getURI()).compareTo(d.getURI());
	}

	public HttpObject getHttpObject() {
		return req.duplicate();
	}

	public String getMethod() {
		return req.getMethod().toString();
	}

	public String getProtocol() {
		return req.getProtocolVersion().toString();
	}

	public String getHost() {
		return req.headers().get("Host");
	}

	public void setContent(String value) {
		req.content().clear();
		req.content().writeBytes(value.getBytes(StandardCharsets.UTF_8));
		req.headers().set("Content-Length", value.length());
	}

	public void setContentType(ContentType type) {
		req.headers().set(CONTENT_TYPE, type.toString());
	}

	public Boolean isJSON() {
		String type = req.headers().get(CONTENT_TYPE);
		if (type == null) {
			type = "";
		}
		return type.contains("application/json");
	}

	public Boolean isHTML() {
		String type = req.headers().get(CONTENT_TYPE);
		if (type == null) {
			type = "";
		}
		return type.contains("text/html");
	}

	public Boolean isXML() {
		String type = req.headers().get(CONTENT_TYPE);
		if (type == null) {
			type = "";
		}
		return type.contains("text/xml");
	}

	public MATCHTYPE getMatchType() {
		return matchType;
	}

	public void setMatchType(MATCHTYPE matchType) {
		this.matchType = matchType;
	}

	public String getURI() {

		return req.getUri();
	}

	public void setURI(String uri) {
		req.setUri(uri);
	}

	public void setHostHeader(String host) {
		req.headers().remove("Host");
		req.headers().set("Host", host);
	}

	public void setMethod(HttpMethod method) {
		req.setMethod(method);
	}

	public void setVersion(HttpVersion httpVersion) {
		req.setProtocolVersion(httpVersion);
	}

	public String getContentType() {
		String contentType = req.headers().get(CONTENT_TYPE);
		if (contentType == null) {
			return "";
		}
		return contentType;
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
		if (req == null) {
			if (other.req != null)
				return false;
		} else if (!this.getURI().equalsIgnoreCase(other.getURI()) || !this.getMethod().equalsIgnoreCase(other.getMethod().toString())
				|| !getContent().equalsIgnoreCase(other.getContent()))
			return false;
		return true;
	}

	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Request" + StringUtil.NEWLINE);
		buf.append("   Method: " + req.getMethod().toString() + StringUtil.NEWLINE);
		buf.append("   URI: " + getURI() + StringUtil.NEWLINE);
		buf.append("   Protocol: " + req.getProtocolVersion().text() + StringUtil.NEWLINE);
		buf.append("   Content-Type: " + req.headers().get(CONTENT_TYPE) + StringUtil.NEWLINE);
		buf.append("   Match Type: " + this.getMatchType().toString() + StringUtil.NEWLINE);
		buf.append("   Matched String: " + this.matchedString + StringUtil.NEWLINE);
		buf.append("   Content: " + StringUtil.NEWLINE);
		
		if (isJSON() || isXML()) {
			buf.append(getContent() + StringUtil.NEWLINE);
		} else {
			buf.append("Cannot be displayed " + StringUtil.NEWLINE);
		}
		return buf.toString();
	}

	public void setMatchString(String matchedString) {
		this.matchedString = matchedString;
	}

}
