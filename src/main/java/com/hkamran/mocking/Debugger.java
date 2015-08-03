package com.hkamran.mocking;

import org.apache.log4j.Logger;

import com.hkamran.mocking.gui.UIEvent;

public class Debugger {

	private final static Logger log = Logger.getLogger(Debugger.class);

	private Boolean state = false;
	public static UIEvent event;
	
	private Response curResponse = null;
	private Request curRequest = null;

	public Response analyze(Request request) {
		log.info("Debugging " + request.hashCode());
		curRequest = request;
		uiEvent();
		synchronized (this) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		log.info("Finished bebugging " + request.hashCode());
		return curResponse;
	}

	public void setResponse(Response response) {
		this.curResponse = response;
		wakeUp();
	}
	
	public Request getRequest() {
		return curRequest;
	}

	public void setState(Boolean state) {
		log.info("Settings debug state " + state);
		if (!state) {
			wakeUp();
		}
		this.state = state;
	}

	private void wakeUp() {
		synchronized (this) {
			this.notifyAll();
		}
	}

	public Boolean getState() {
		return state;
	}

	public static void setUIEventHandler(UIEvent uiEvent) {
		event = uiEvent;
	}
	
	public void uiEvent() {
		if (event != null) {
			event.event(this);
		}
	}
}
