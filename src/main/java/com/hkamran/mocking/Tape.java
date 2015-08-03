package com.hkamran.mocking;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.http.entity.ContentType;
import org.apache.log4j.Logger;
import org.json.JSONObject;

import com.hkamran.mocking.Request.MATCHTYPE;
import com.hkamran.mocking.gui.UIEvent;

public class Tape {

	private final static Logger log = Logger.getLogger(Main.class);
	private Map<Request, List<Response>> tape = Collections.synchronizedMap(new HashMap<Request, List<Response>>());
	
	public static UIEvent event;
	
	private class Constants {
		private static final String MATCHED_STRING = "matchedString";
		private static final String MATCH_TYPE = "matchType";
		private static final String STATUS = "status";
		private static final String URI = "uri";
		private static final String METHOD = "method";
		private static final String CONTENT = "content";
		private static final String PROTOCOL = "protocol";
		private static final String TYPE = "type";
	}

	public void put(Request request, Response response) {
		if (!tape.containsKey(request)) {
			tape.put(request, new ArrayList<Response>());
		}

		List<Response> responses = tape.get(request);
		responses.add(response);

		updateTreeUI();
	}

	public void put(Request request, List<Response> responses) {
		if (responses == null) {
			tape.put(request, new ArrayList<Response>());
		} else {
			tape.put(request, responses);
		}
		updateTreeUI();
	}

	private void updateTreeUI() {
		if (event != null) {
			event.event(this);
		}
	}

	public List<Request> getRequests() {
		return new ArrayList<Request>(tape.keySet());
	}

	public Request getRequest(String hashCode) {
		for (Request request : tape.keySet()) {
			String reqURI = request.hashCode() + "";
			if (reqURI.equalsIgnoreCase(hashCode)) {
				return request;
			}
		}
		return null;
	}

	public void remove(Request request) {
		tape.remove(request);

	}

	public Response getResponse(final Request request) {
		Request key = null;
		for (Request temp : tape.keySet()) {
			if (temp.getMatchType() == MATCHTYPE.request) {
				if (temp.equals(request)) {
					key = temp;
					break;
				}
			} else if (temp.getMatchType() == MATCHTYPE.content) {
				// Match the request content with the tapes matching content
				if (request.getContent().contains(temp.matchedString)) {
					key = temp;
					break;
				}
			}
		}

		if (key == null) {
			log.info("Cannot find request " + request.hashCode() + " in tape");
			return null;
		}

		List<Response> responses = tape.get(key);

		if (responses != null && responses.size() > 0) {
			if (key.counter >= responses.size()) {
				key.counter = 0;
			}
			Response response = tape.get(key).get(key.counter++);
			log.info("Found response for request " + request.hashCode() + " in tape");
			return response;
		} else {
			log.info("Cannot find response for request " + request.hashCode() + " in tape");
			return null;
		}
	}

	public List<Response> getResponses(Request request) {
		return tape.get(request);
	}

	public List<Response> getResponses(String hashCode) {
		Request request = getRequest(hashCode);
		return tape.get(request);
	}

	public static void setUIEventHandler(UIEvent e) {
		Tape.event = e;
	}

	public void export(String path) throws IOException {
		JSONObject mockedCalls = new JSONObject();
		try {
			for (Request request : tape.keySet()) {
				List<Response> responses = tape.get(request);
				JSONObject requestJSON = new JSONObject();

				requestJSON.put(Constants.URI, request.getURI());
				requestJSON.put(Constants.METHOD, request.getMethod());
				requestJSON.put(Constants.CONTENT, request.getContent());
				requestJSON.put(Constants.PROTOCOL, request.getProtocol());
				requestJSON.put(Constants.TYPE, request.getContentType());
				requestJSON.put(Constants.MATCH_TYPE, request.getMatchType().toString());
				requestJSON.put(Constants.MATCHED_STRING, request.matchedString);

				JSONObject responsesJSON = new JSONObject();
				for (Integer i = 0; i < responses.size(); i++) {
					Response response = responses.get(i);
					JSONObject responseJSON = new JSONObject();
					responseJSON.put(Constants.STATUS, response.getStatus());
					responseJSON.put(Constants.TYPE, response.getContentType());
					responseJSON.put(Constants.PROTOCOL, response.getProtocol());
					responseJSON.put(Constants.CONTENT, response.getContent());
					responsesJSON.put(i.toString(), responseJSON);
				}
				requestJSON.put("responses", responsesJSON);
				mockedCalls.put(request.hashCode() + "", requestJSON);
			}

			File file = new File(path);
			FileUtils.writeByteArrayToFile(file, mockedCalls.toString(4).getBytes());
			log.info("Exported tape " + tape.hashCode() + " to " + path);
		} catch (Exception e) {
			log.info("Unable to export tape due to " + e.getMessage());
			e.printStackTrace();
		}

	}

	public static Tape load(String path) throws IOException {
		try {
			Tape tape = new Tape();

			File file = new File(path);
			String state = FileUtils.readFileToString(file);
			JSONObject mockedCalls = new JSONObject(state);

			@SuppressWarnings("rawtypes")
			Iterator calls = mockedCalls.keys();
			while (calls.hasNext()) {
				String hashCode = (String) calls.next();
				JSONObject mockedCall = mockedCalls.getJSONObject(hashCode);

				String uri = mockedCall.getString(Constants.URI);
				String method = mockedCall.getString(Constants.METHOD);
				String content1 = mockedCall.getString(Constants.CONTENT);
				String protocol = mockedCall.getString(Constants.PROTOCOL);
				String type1 = ContentType.WILDCARD.toString();
				if (mockedCall.has(Constants.TYPE)) {
					type1 = mockedCall.getString(Constants.TYPE);
				}
				MATCHTYPE matchType = MATCHTYPE.valueOf(mockedCall.getString(Constants.MATCH_TYPE));
				String matchedString = mockedCall.getString(Constants.MATCHED_STRING);

				JSONObject responsesJSON = mockedCall.getJSONObject("responses");
				Integer length = responsesJSON.length();
				Request request = new Request(new DefaultFullHttpRequest(HttpVersion.valueOf(protocol), HttpMethod.valueOf(method), uri));
				request.setContent(content1);
				request.setContentType(ContentType.parse(type1));
				request.setMatchType(matchType);
				request.setMatchString(matchedString);

				for (Integer index = 0; index < length; index++) {
					JSONObject responseJSON = responsesJSON.getJSONObject(index.toString());
					Integer status = responseJSON.getInt(Constants.STATUS);
					String type = responseJSON.getString(Constants.TYPE);
					String content = responseJSON.getString(Constants.CONTENT);

					String resProtocol = responseJSON.getString(Constants.PROTOCOL);

					Response response = new Response(
							new DefaultFullHttpResponse(HttpVersion.valueOf(resProtocol), HttpResponseStatus.valueOf(status)));
					response.setContent(content, ContentType.parse(type));

					tape.put(request, response);
				}

			}
			log.info("Loaded tape " + tape.hashCode() + " from " + path);
			return tape;
		} catch (Exception e) {
			log.info("Unable to load tape due to " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

}
