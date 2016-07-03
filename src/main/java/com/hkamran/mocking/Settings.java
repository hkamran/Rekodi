package com.hkamran.mocking;

import org.json.JSONObject;

import com.hkamran.mocking.FilterManager.State;

public class Settings {

	public State state;
	public String host;
	public Integer port;
	public Boolean redirect;

	public static Settings parseJSON(String source) {
		JSONObject json = new JSONObject(source);

		String proxyState = json.getString("state");
		Boolean redirectState = json.getBoolean("redirect");
		String host = json.getString("host");
		Integer port = json.getInt("port");

		Settings settings = new Settings();

		settings.state = FilterManager.State.valueOf(proxyState);
		settings.redirect = redirectState;
		settings.host = host;
		settings.port = port;

		return settings;
	}

	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("state", this.state);
		json.put("redirect", this.redirect);
		json.put("port", this.port);
		json.put("host", this.host);

		return json;
	}

}
