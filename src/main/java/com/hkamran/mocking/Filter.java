package com.hkamran.mocking;

import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;

import com.hkamran.mocking.servers.WebSocket;

/**
 * This class handles the logic of filtering requests
 * and responses. It determines whether to return a mocked response, record
 * or just allow through each request.
 * 
 * @author Hooman Kamran
 */
public class Filter extends HttpFiltersSourceAdapter {

	private final static Logger log = LogManager.getLogger(Filter.class);
	private static final int MAX_SIZE = 8388608;

	private Tape tape;

	public Set<Request> tmp = new HashSet<Request>();
	private List<Event> events = new ArrayList<Event>();
	private Integer counter = 0;
	
	public State state = Filter.State.PROXY;
	public String host = "localhost";
	public Integer port = 80;
	public Boolean redirect = true;
	public Boolean recordMock = false;
	public Integer id;
	
	public static enum State {
		MOCK, PROXY, RECORD;
	}

	public Filter(Integer id) {
		this.id = id;
		this.tape = new Tape();
	}

	@Override
	public HttpFilters filterRequest(HttpRequest originalRequest) {

		return new HttpFiltersAdapter(originalRequest) {

			Request req;
			Response res;
			StopWatch watch;

			@Override
			public HttpResponse clientToProxyRequest(HttpObject httpObject) {
				log.info("handling pre-request");
				try {
					if (httpObject instanceof FullHttpRequest) {
						FullHttpRequest httpFullObj = (FullHttpRequest) httpObject;
						if (redirect) {
							HttpHeaders headers = httpFullObj.headers();
							headers.set("Host", host + ":" + port); 
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return null;

			}

			@Override
			public HttpResponse proxyToServerRequest(HttpObject httpObject) {
				log.info("handling post-request");
				try {
					if (httpObject instanceof FullHttpRequest) {

						FullHttpRequest httpFullObj = (FullHttpRequest) httpObject;
						req = new Request(httpFullObj, state);

						log.info("Request incoming: " + req.hashCode());
						watch = new StopWatch();
						watch.start();

						if (state == State.PROXY) {
							// No need
						} else if (state == State.MOCK) {
							if (!tmp.contains(req) && tape.hasRequest(req)) {
								HttpResponse response = sendToMock(req, watch);
								if (!redirect && response == null) {
									return handleNoRedirect();
								}
								return response;
							} else {
								if (recordMock) {
									tmp.add(req);
								}
							}
						} else if (state == State.RECORD) {
							// No Need
						}

						if (!redirect) {
							return handleNoRedirect();
						}

						addEvent(counter, res, req, watch);
					} else {
						throw new RuntimeException("HttpObject is not " + FullHttpRequest.class);
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return null;
			}

			private HttpResponse handleNoRedirect() {
				Map<String, String> headers = new HashMap<String, String>();
				String content = "";
				String protocol = "HTTP/1.1";
				Integer status = 500;
				State resState = state;
				
				Response response = new Response(headers, content, protocol, status, resState);
				this.res = response;
				this.res.setParent(req.hashCode());
				if (state == State.RECORD) {
					sendToRecorder(res, req);
				}
				
				try {
					watch.stop();
				} catch (IllegalStateException e) {}
				
				addEvent(counter, res, req, watch);
				return response.getHTTPObject();
			}

			@Override
			public HttpObject serverToProxyResponse(HttpObject httpObject) {
				log.info("handling post-response");
				try {
					if (httpObject instanceof FullHttpResponse) {

						FullHttpResponse httpFullObj = (FullHttpResponse) httpObject;
						res = new Response(httpFullObj, state);
						res.setParent(req.hashCode());
						try {
							watch.stop();
						} catch (IllegalStateException e) {

						}
						log.info("Response outgoing: " + res.hashCode() + " for " + req.hashCode());

						if (state == State.PROXY) {
							// No need.
						} else if (state == State.MOCK) {
							if (recordMock) {
								req.setState(State.RECORD);
								res.setState(State.RECORD);
								sendToRecorder(res, req);
							}
						} else if (state == State.RECORD) {
							sendToRecorder(res, req);
						}

						addEvent(counter++, res, req, watch);

					} else {
						throw new RuntimeException("HttpObject is not " + FullHttpResponse.class);
					}
					return httpObject;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}

		};
	}

	private void addEvent(Integer id, Response res, Request req, StopWatch watch) {
		Long duration = TimeUnit.MILLISECONDS.toMillis(watch.getTime());

		Event event = new Event(id, req, res, new Date(watch.getStartTime()), duration, state);

		Payload payload = new Payload(this.id, Payload.Action.INSERT, Payload.Type.EVENT, event);
		WebSocket.broadcast(payload);
	}

	/**
	 * Request Handlers
	 * 
	 * @param watch
	 */

	public HttpResponse sendToMock(final Request request, StopWatch watch) {
		if (tape == null) {
			return null;
		}

		Response response = tape.getResponse(request);

		if (response != null) {
			watch.stop();
			log.info("Mocked Response outgoing: " + response.hashCode() + " for " + request.hashCode());
			addEvent(counter++, response, request, watch);
			return (HttpResponse) response.getHTTPObject();
		} else {
			log.info("Mocked Response outgoing: null for " + request.hashCode());
			addEvent(counter, response, request, watch);
			return null;
		}
	}

	public HttpResponse sendToRecorder(Response res, Request req) {
		tape.put(req, res);
		return null;
	}

	/**
	 * Setter and Getters
	 */

	public void setState(State state) {
		log.info("State set to " + state.toString());
		if (state != State.MOCK) {
			this.tmp.clear();
		}
		this.state = state;
	}
	
	public void setRecordMock(Boolean val) {
		log.info("Setting RecordMocked: " + val);
		this.recordMock = val;
	}
	
	public Boolean getRecordMock() {
		return this.recordMock;
	}

	public Tape getTape() {
		return this.tape;
	}

	public void setTape(Tape tape) {
		if (tape == null) {
			return;
		}
		log.info("Settings Tape: " + tape.hashCode());
		this.tape = tape;
	}

	public void setRedirectInfo(String host, Integer port) {
		this.host = host;
		this.port = port;
		log.info("Setting Redirect: " + host + ":" + port);
	}

	public void setRedirectState(boolean state) {
		log.info("Redirecting state: " + state);
		this.redirect = state;
	}

	public Boolean getRedirectState() {
		return this.redirect;
	}

	public String getHost() {
		return this.host;
	}

	public Integer getPort() {
		return this.port;
	}

	public State getState() {
		return this.state;
	}

	public List<Event> getEvents() {
		List<Event> events = this.events;
		this.events = new ArrayList<Event>();
		return events;
	}
	
	@Override
	public int getMaximumRequestBufferSizeInBytes() {
		return MAX_SIZE;
	}

	@Override
	public int getMaximumResponseBufferSizeInBytes() {
		return MAX_SIZE;
	}
	
	public JSONObject toJSON() {
		JSONObject json = new JSONObject();
		json.put("host", this.host);
		json.put("port", this.port);
		json.put("redirect", this.redirect);
		json.put("state", this.state);
		json.put("id", this.id);
		json.put("recordMock", this.recordMock);
		return json;
	}
	
	public static Filter parseJSON(String source) {
		JSONObject json = new JSONObject(source);
		
		Integer id = json.getInt("id");
		String host = json.getString("host");
		Integer port = json.getInt("port");
		Boolean redirect = json.getBoolean("redirect");
		State state = State.valueOf(json.getString("state"));
		Boolean recordMock = json.getBoolean("recordMock");
		
		Filter filter = new Filter(id);
		filter.setRedirectInfo(host, port);
		filter.setRedirectState(redirect);
		filter.setState(state);
		filter.setRecordMock(recordMock);
		return filter;
	}

}