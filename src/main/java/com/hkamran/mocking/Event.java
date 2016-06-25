package com.hkamran.mocking;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONObject;

import com.hkamran.mocking.FilterManager.State;


public class Event {
	public Response response;
	public Request request;
	public String start;
	public Long duration;
	public String state;

	public Event(Request req, Response res, Date date, Long duration, State state) {
		this.request = req;
		this.response = res;
		this.duration = duration;
		this.state = state.toString();
		
		DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss a");
		this.start = dateFormat.format(date);
	}
	
	public JSONObject toJSON() {
		JSONObject result = new JSONObject();
		
		JSONObject request = this.request.toJSON();
		JSONObject response = this.response.toJSON();
		
		result.put("request", request);
		result.put("response", response); 

		result.put("start", start);
		result.put("duration", duration);
		result.put("state", state);
		
		return result;
	}

}
