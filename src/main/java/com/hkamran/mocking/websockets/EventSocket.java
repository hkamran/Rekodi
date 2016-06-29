package com.hkamran.mocking.websockets;

import java.util.ArrayList;
import java.util.List;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;

@ClientEndpoint
@ServerEndpoint(value = "/events")
public class EventSocket {

	private final static Logger log = Logger.getLogger(EventSocket.class);
	List<Session> sessions = new ArrayList<Session>();

	@OnOpen
	public void OnWebSocketConnect(Session session) {
		sessions.add(session);
		log.info("Socket Opened: " + session.getId());
	}

	@OnMessage
	public void onWebSocketText(String message) {
		System.out.println("Received TEXT message: " + message);
	}

	@OnClose
	public void onWebSocketClose(Session session) {
		
		System.out.println("Socket Closed: " + session.getId());
	}

	@OnError
	public void onWebSocketError(Throwable cause) {
		cause.printStackTrace(System.err);
	}

}
