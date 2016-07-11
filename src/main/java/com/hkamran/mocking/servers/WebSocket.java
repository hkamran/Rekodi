package com.hkamran.mocking.servers;

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

import com.hkamran.mocking.Filter;
import com.hkamran.mocking.Payload;
import com.hkamran.mocking.Proxies;
import com.hkamran.mocking.Proxy;
import com.hkamran.mocking.Request;
import com.hkamran.mocking.Response;
import com.hkamran.mocking.Tape;
import com.hkamran.mocking.Filter.State;
import com.hkamran.mocking.Payload.Action;

@ClientEndpoint
@ServerEndpoint(value = "/")
public class WebSocket {

	private static Proxies proxies = new Proxies();
	private final static Logger log = Logger.getLogger(WebSocket.class);
	static List<Session> sessions = new ArrayList<Session>();

	@OnOpen
	public void OnWebSocketConnect(Session session) {
		sessions.add(session);
		log.info("Socket Opened: " + session.getId());
		
		session.setMaxIdleTimeout(600000);
		
		WebSocket.send(session, Payload.create(-1, 
				Action.UPDATE, 
				Payload.Type.PROXIES, WebSocket.proxies));
		
		for (Integer id : WebSocket.proxies.keySet()) {
			Proxy proxy = WebSocket.proxies.get(id);
			
			Filter filter = proxy.getFilter();
			
			WebSocket.send(session, Payload.create(id, 
					Payload.Action.UPDATE, 
					Payload.Type.FILTER, filter));
			
			WebSocket.send(session, Payload.create(id, 
					Payload.Action.UPDATE,
					Payload.Type.TAPE, filter.getTape()));
			
		}
		
	}
	
	@OnMessage
	public void onWebSocketText(String message, Session session) {
		
		Payload payload = Payload.parseJSON(message);
		System.out.println("Received TEXT message: " + message);
		
		if (payload.type == Payload.Type.FILTER) {
			handleFilterUpdate(payload);
		} else if (payload.type == Payload.Type.TAPE) {
			handleTapeUpdate(payload);
		} else if (payload.type == Payload.Type.REQUEST) {
			handleRequestUpdate(session, payload);
		} else if (payload.type == Payload.Type.RESPONSE) {
			handleResponseUpdate(session, payload);
		} else if (payload.type == Payload.Type.PROXY) {
			handleProxyUpdate(session, payload);
		}
	}
	
	private void handleProxyUpdate(Session session, Payload payload) {
		Proxy proxy = (Proxy) payload.obj;
		
		if (payload.action == Payload.Action.INSERT) {
			Integer id = WebSocket.proxies.add(proxy.name, proxy.port);
			Proxy createProxy = WebSocket.proxies.get(id);
			
			Payload proxies = Payload.create(id, 
					Payload.Action.INSERT, 
					Payload.Type.PROXY, createProxy);
			
			WebSocket.broadcast(proxies);
			
		} else {
			Proxy updateProxy = WebSocket.proxies.get(proxy.id);
			if (updateProxy == null) {
				log.info("Unable to find proxy id " + proxy.id);
				return;
			}
			
			if (payload.action == Payload.Action.DELETE) {
				synchronized (proxies) {
					if (WebSocket.proxies.size() == 1) {
						return;
					}
				
					Proxy originalProxy = WebSocket.proxies.get(proxy.id);					
					Payload payloadUpdate = Payload.create(-1, 
							Payload.Action.DELETE, 
							Payload.Type.PROXY, originalProxy);
					WebSocket.broadcast(payloadUpdate);
					log.info("Deleting proxy " + originalProxy.id + ":" + originalProxy.name);
					
					WebSocket.proxies.remove(proxy.id);
					return;
				}
			} else if (payload.action == Payload.Action.UPDATE) {
				Proxy curProxy = WebSocket.proxies.get(proxy.id);
				
				String name = curProxy.name;
				Integer port = curProxy.port;
				
				curProxy.name = name;
				
				if (port != curProxy.port) {
					curProxy.port = port;
					curProxy.stop();
					curProxy.start();
					
				}
				
				Payload proxies = Payload.create(-1, 
						Payload.Action.UPDATE, 
						Payload.Type.PROXIES, WebSocket.proxies);
				
				WebSocket.broadcast(proxies);
				log.info("Creating proxy " + curProxy.id + ":" + curProxy.name);
				
				return;
			}
		}
	
	}

	private void handleResponseUpdate(Session session, Payload payload) {
		if (!(payload.obj instanceof Response)) {
			return;
		}
		
		if (payload.action == Payload.Action.UPDATE) {
			Response response = (Response) payload.obj;
			
			Integer id = payload.id;
			Proxy proxy = proxies.get(id);
			Filter filter = proxy.getFilter();
			Tape tape = filter.getTape();
			
			Request request = tape.getRequest(response.getParent().toString());
			
			List<Response> responses = tape.getResponses(request);
			responses.set(response.getId(), response);
	
			Payload updateTape = Payload.create(id, 
					Payload.Action.UPDATE, 
					Payload.Type.TAPE, filter.getTape());
			WebSocket.broadcast(updateTape);	
			
			Payload updatedResponse = Payload.create(id, 
					Payload.Action.UPDATE, 
					Payload.Type.RESPONSE, response);
			WebSocket.broadcast(updatedResponse);
		}
	}

	private void handleRequestUpdate(Session session, Payload payload) {
		if (!(payload.obj instanceof Request)) {
			return;
		}
		
		if (payload.action == Payload.Action.UPDATE) {
			Request request = (Request) payload.obj;
			Integer id = payload.id;
			Proxy proxy = proxies.get(id);
			Filter filter = proxy.getFilter();
			
			filter.getTape().setRequest(request.pastID.toString(), request);
	
			Payload updateTape = Payload.create(id, 
					Payload.Action.UPDATE, 
					Payload.Type.TAPE, filter.getTape());
			WebSocket.broadcast(updateTape);	
			
			Payload updatedRequest = Payload.create(id, 
					Payload.Action.UPDATE, 
					Payload.Type.REQUEST, request);
			WebSocket.broadcast(updatedRequest);
		}
	}

	private void handleTapeUpdate(Payload payload) {
		if (!(payload.obj instanceof Tape)) {
			return;
		}
		
		if (payload.action == Payload.Action.UPDATE) {
			Tape tape = (Tape) payload.obj;
			
			Integer id = payload.id;
			Proxy proxy = proxies.get(id);
			Filter filter = proxy.getFilter();
			
			filter.setTape(tape);
			Payload update = Payload.create(id, 
					Payload.Action.UPDATE, 
					Payload.Type.TAPE, filter.getTape());
			
			log.info("Updating tape " + payload.id);
			WebSocket.broadcast(update);
		}
	}

	private void handleFilterUpdate(Payload payload) {		
		if (!(payload.obj instanceof Filter)) {
			return;
		}
		
		if (payload.action == Payload.Action.UPDATE) {
			Filter updatedFilter = (Filter) payload.obj;
			
			String host = updatedFilter.host;
			Integer port = updatedFilter.port;
			Boolean redirect = updatedFilter.getRedirectState();
			State state = updatedFilter.getState();
			
			Integer id = payload.id;
			Proxy proxy = proxies.get(id);
			
			Filter filter = proxy.getFilter();
			filter.setRedirectInfo(host, port);
			filter.setRedirectState(redirect);
			filter.setState(state);
			
			Payload filterPayload = Payload.create(id, 
					Payload.Action.UPDATE, 
					Payload.Type.FILTER, filter);
			
			WebSocket.broadcast(filterPayload);
		}
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
	
	public static void send(Session session, Payload payload) {
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
	
	public static void setProxyManager(Proxies manager) {
		WebSocket.proxies = manager;
	}

}
