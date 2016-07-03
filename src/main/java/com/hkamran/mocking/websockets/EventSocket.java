package com.hkamran.mocking.websockets;

import java.io.EOFException;
import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import javax.websocket.ClientEndpoint;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import org.apache.log4j.Logger;
import org.json.JSONException;

import com.hkamran.mocking.FilterManager;
import com.hkamran.mocking.Request;
import com.hkamran.mocking.Response;
import com.hkamran.mocking.Settings;
import com.hkamran.mocking.Tape;

@ClientEndpoint
@ServerEndpoint(value = "/")
public class EventSocket {

	private static FilterManager filter;
	private final static Logger log = Logger.getLogger(EventSocket.class);
	static List<Session> sessions = new ArrayList<Session>();

	@OnOpen
	public void OnWebSocketConnect(Session session) {
		sessions.add(session);
		log.info("Socket Opened: " + session.getId());
		send(session, Payload.create(Payload.Type.SETTINGS, filter.settings));
		send(session, Payload.create(Payload.Type.TAPE, filter.getTape()));
	}
	
	@OnMessage
	public void onWebSocketText(String message, Session session) {
		
		Payload payload = Payload.parseJSON(message);
		System.out.println("Received TEXT message: " + message);
		
		if (payload.type == Payload.Type.SETTINGS) {
			handleSettingsUpdate(payload);
		} else if (payload.type == Payload.Type.TAPE) {
			handleTapeUpdate(payload);
		} else if (payload.type == Payload.Type.REQUEST) {
			handleRequestUpdate(session, payload);
		} else if (payload.type == Payload.Type.RESPONSE) {
			handleResponseUpdate(session, payload);
		}
	}
	
	private void handleResponseUpdate(Session session, Payload payload) {
		Response response = (Response) payload.obj;
		
		Tape tape = filter.getTape();
		Request request = tape.getRequest(response.getParent().toString());
		List<Response> responses = tape.getResponses(request);
		responses.set(response.getId(), response);

		Payload updateTape = Payload.create(Payload.Type.TAPE, filter.getTape());
		EventSocket.broadcast(updateTape);	
		
		Payload updatedResponse = Payload.create(Payload.Type.RESPONSE, response);
		EventSocket.broadcast(updatedResponse);
	}

	private void handleRequestUpdate(Session session, Payload payload) {
		Request request = (Request) payload.obj;
		filter.getTape().setRequest(request.pastID.toString(), request);

		Payload updateTape = Payload.create(Payload.Type.TAPE, filter.getTape());
		EventSocket.broadcast(updateTape);	
		
		Payload updatedRequest = Payload.create(Payload.Type.REQUEST, request);
		EventSocket.broadcast(updatedRequest);
	}

	private void handleTapeUpdate(Payload payload) {
		Tape tape = (Tape) payload.obj;
		filter.setTape(tape);
		Payload update = Payload.create(Payload.Type.TAPE, filter.getTape());
		EventSocket.broadcast(update);
	}

	private void handleSettingsUpdate(Payload payload) {
		filter.setSettings((Settings) payload.obj);
		EventSocket.broadcast(Payload.create(Payload.Type.SETTINGS, filter.settings));
	}

	@OnClose
	public void onWebSocketClose(Session session) {
		System.out.println("Socket Closed: " + session.getId());
	}

	@OnError
	public void onWebSocketError(Throwable cause) {
		if (cause instanceof EOFException) {
			return;
		} else if (cause instanceof SocketTimeoutException) {
			return;
		}
		cause.printStackTrace(System.err);
	}
	
	public void send(Session session, Payload payload) {
		try {
			session.getBasicRemote().sendText(payload.toJSON().toString(2));
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void broadcast(Payload payload) {
		for (Session session : sessions) {
			if (session.isOpen()) {
				try {
					session.getBasicRemote().sendText(payload.toJSON().toString());
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	public static void setFilter(FilterManager filter) {
		EventSocket.filter = filter;
	}

}
