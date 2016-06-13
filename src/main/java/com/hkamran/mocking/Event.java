package com.hkamran.mocking;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.hkamran.mocking.FilterManager.State;


public class Event {
	public Response response;
	public Request request;
	public String start;
	public Long duration;
	public String isRecordered;

	public Event(Request req, Response res, Date date, Long duration, State state) {
		this.request = req;
		this.response = res;
		this.duration = duration;
		this.isRecordered = state.toString();
		
		DateFormat dateFormat = new SimpleDateFormat("hh:mm:ss a");
		this.start = dateFormat.format(date);
	}

}
