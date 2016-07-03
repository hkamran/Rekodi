package com.hkamran.mocking.websockets;

import org.json.JSONObject;

import com.hkamran.mocking.Event;
import com.hkamran.mocking.FilterManager;
import com.hkamran.mocking.Request;
import com.hkamran.mocking.Response;
import com.hkamran.mocking.Settings;
import com.hkamran.mocking.Tape;

public class Payload {

	public static enum Type {
		TAPE, REQUEST, RESPONSE, SETTINGS, NONE, EVENT
	}
	
	Type type;
	Object obj;
	
	public Payload(Type type, Object obj) {
		this.type = type;
		this.obj = obj;
	}
	
	public JSONObject toJSON() {
		
		JSONObject json = new JSONObject();
		json.put("type", this.type);
		
		JSONObject payload;
		
		if (obj instanceof Response) {
			Response response = (Response) obj;
			payload = response.toJSON();
		} else if (obj instanceof Request) {
			Request request = (Request) obj;
			payload = request.toJSON();
		} else if (obj instanceof Event) {
			Event event = (Event) obj;
			payload = event.toJSON();
		} else if (obj instanceof Tape) {
			Tape tape = (Tape) obj;
			payload = tape.toJSON();
		} else if (obj instanceof Settings) {
			Settings settings = (Settings) obj;
			payload = settings.toJSON();
		} else {
			this.type = Type.NONE;
			payload = new JSONObject();
			json.put("type", this.type);
		}
		
		json.put("message", payload);
		
		return json;
	}
	
	public static Payload parseJSON(String source) {
		JSONObject json = new JSONObject(source);
		
		Type type = Type.valueOf(json.getString("type"));
		String message = json.get("message").toString();
		
		Object obj = null;
		if (type == Type.TAPE) {
			obj = Tape.parseJSON(message);
		} else if (type == Type.EVENT) {
		} else if (type == Type.REQUEST) {
			obj = Request.parseJSON(message);
		} else if (type == Type.RESPONSE) {
			obj = Response.parseJSON(message);
		} else if (type == Type.SETTINGS) {
			obj = Settings.parseJSON(message);
		} else {
			
		}
		Payload payload = new Payload(type, obj);
		return payload;	
	}
	
	public static Payload create(Type type, Object payload) {
		return new Payload(type, payload);
	}
	
}
