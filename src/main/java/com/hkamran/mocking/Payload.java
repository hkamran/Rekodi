package com.hkamran.mocking;

import org.json.JSONObject;

/**
 * This class is used to communicate between the web client and the recording proxy.
 * Both can request to insert, delete, update certain objects from their systems.
 *  
 * @author Hooman Kamran
 */
public class Payload {

	public static enum Type {
		TAPE, REQUEST, RESPONSE, FILTER, NONE, EVENT, PROXY, PROXIES
	}
	
	public static enum Action {
		UPDATE, DELETE, INSERT
	}
	
	public Type type;
	public Object obj;
	public Integer id;
	public Action action;
	
	public Payload(Integer id, Action action, Type type, Object obj) {
		this.type = type;
		this.obj = obj;
		this.id = id;
		this.action = action;
	}
	
	public JSONObject toJSON() {
		
		JSONObject json = new JSONObject();
		json.put("type", this.type);
		json.put("id", id);
		json.put("action", this.action);
		
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
		} else if (obj instanceof Filter) {
			Filter filter = (Filter) obj;
			payload = filter.toJSON();
		} else if (obj instanceof Proxy) {
			Proxy proxy = (Proxy) obj;
			payload = proxy.toJSON();
		} else if (obj instanceof Proxies) {
			Proxies proxies = (Proxies) obj;
			payload = proxies.toJSON();			
		} else {
			throw new RuntimeException("Unable to transform payload to json" + this.obj.getClass());
		}
		
		json.put("message", payload);
		
		return json;
	}
	
	public static Payload parseJSON(String source) {
		JSONObject json = new JSONObject(source);
		
		Type type = Type.valueOf(json.getString("type"));
		String message = json.get("message").toString();
		Integer id = json.getInt("id");
		Action action = Action.valueOf(json.getString("action"));
		
		Object obj = null;
		if (type == Type.TAPE) {
			obj = Tape.parseJSON(message);
		} else if (type == Type.EVENT) {
		} else if (type == Type.REQUEST) {
			obj = Request.parseJSON(message);
		} else if (type == Type.RESPONSE) {
			obj = Response.parseJSON(message);
		} else if (type == Type.PROXY) {
			obj = Proxy.parseJSON(message);	
		} else if (type == Type.FILTER) {
			obj = Filter.parseJSON(message);	
		} else {
			throw new RuntimeException("Unable to transform payload to obj" + json.toString(2));
		}
		Payload payload = new Payload(id, action, type, obj);
		return payload;	
	}
	
	public static Payload create(Integer id, Action action, Type type, Object payload) {
		return new Payload(id, action, type, payload);
	}
	
}
