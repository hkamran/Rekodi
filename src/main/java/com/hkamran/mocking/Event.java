package com.hkamran.mocking;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import com.hkamran.mocking.Filter.State;

/**
 * This class holds the information of that occurred when
 * the clients sends a requests and gets a response.
 * 
 * @author Hooman Kamran
 */
public class Event {
	public Response response;
	public Request request;
	public String start;
	public Long duration;
	public String state;
	public Integer id;
	
	public Event(Integer id, Request req, Response res, Date date, Long duration, State state) {
		this.id = id;
		this.request = req;
		this.response = res;
		this.duration = duration;
		this.state = state.toString();
		
		DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss a");
		this.start = dateFormat.format(date);
	}
	
	public JSONObject toJSON() {
		JSONObject result = new JSONObject();
		
		JSONObject request = new JSONObject();
		if (this.request != null) {
			request = this.request.toJSON();
		}
		JSONObject response = new JSONObject();
		if (this.response != null) {
			response = this.response.toJSON();
		}
		
		result.put("id", id);
		result.put("request", request);
		result.put("response", response); 

		result.put("start", start);
		result.put("duration", duration);
		result.put("state", state);
		
		return result;
	}

}
