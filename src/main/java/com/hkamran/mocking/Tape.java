package com.hkamran.mocking;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.hkamran.mocking.Request.MATCHTYPE;

public class Tape {

	private final static Logger log = Logger.getLogger(Tape.class);
	private Map<Request, List<Response>> tape = Collections.synchronizedMap(new HashMap<Request, List<Response>>());
	
	public static class Constants {
		public static final String MATCHED_STRING = "matchedString";
		public static final String MATCH_TYPE = "matchType";
		public static final String STATUS = "status";
		public static final String URI = "uri";
		public static final String METHOD = "method";
		public static final String CONTENT = "content";
		public static final String PROTOCOL = "protocol";
		public static final String TYPE = "type";
	}

	public void put(Request request, Response response) {
		if (!tape.containsKey(request)) {
			tape.put(request, new ArrayList<Response>());
		}

		List<Response> responses = tape.get(request);
		response.setId(responses.size());
		responses.add(response);

	}

	public void put(Request request, List<Response> responses) {
		if (responses == null) {
			tape.put(request, new ArrayList<Response>());
		} else {
			tape.put(request, responses);
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
	
	public void setRequest(String hashCode, Request request) {
		Request original = getRequest(hashCode);
		if (original == null) {
			throw new RuntimeException("Unable to find request " + hashCode);
		}
		List<Response> responses = getResponses(original);
		for (Response response : responses) {
			response.setParent(request.hashCode());
		}
		
		remove(original);
		this.put(request, responses);
		
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

	public JSONObject toJSON() {
		JSONObject mockedCalls = new JSONObject();
		for (Request request : tape.keySet()) {
			List<Response> responses = tape.get(request);
			JSONObject requestJSON = new JSONObject();

			JSONArray responsesJSON = new JSONArray();
			for (Integer i = 0; i < responses.size(); i++) {
				Response response = responses.get(i);
				responsesJSON.put(response.toJSON());
			}
			requestJSON.put("responses", responsesJSON);
			requestJSON.put("request", request.toJSON());
			mockedCalls.put(request.hashCode() + "", requestJSON);
		}
		return mockedCalls;
	}
	
	public static Tape parseJSON(String source) {
		Tape tape = new Tape();
		JSONObject mockedCalls = new JSONObject(source);

		@SuppressWarnings("rawtypes")
		Iterator calls = mockedCalls.keys();
		while (calls.hasNext()) {
			String hashCode = (String) calls.next();
			JSONObject mockedCall = mockedCalls.getJSONObject(hashCode);


			Request request = Request.parseJSON(mockedCall.getJSONObject("request").toString());
			JSONObject responsesJSON = mockedCall.getJSONObject("responses");
			Integer length = responsesJSON.length();

			for (Integer index = 0; index < length; index++) {
				JSONObject responseJSON = responsesJSON.getJSONObject(index.toString());
				Response response = Response.parseJSON(responseJSON.toString());
				tape.put(request, response);
			}
		}
		
		return tape;
	}

	
	public void export(String path) throws IOException {
		try {
			File file = new File(path);
			FileUtils.writeByteArrayToFile(file, toJSON().toString(4).getBytes());
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
			Tape.parseJSON(state);
			
			log.info("Loaded tape " + tape.hashCode() + " from " + path);
			return tape;
		} catch (Exception e) {
			log.info("Unable to load tape due to " + e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

}
