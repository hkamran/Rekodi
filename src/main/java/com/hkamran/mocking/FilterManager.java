package com.hkamran.mocking;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.time.StopWatch;
import org.apache.log4j.Logger;
import org.json.JSONObject;
import org.littleshoot.proxy.ChainedProxy;
import org.littleshoot.proxy.ChainedProxyAdapter;
import org.littleshoot.proxy.ChainedProxyManager;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;

import com.hkamran.mocking.websockets.EventSocket;
import com.hkamran.mocking.websockets.Payload;

public class FilterManager extends HttpFiltersSourceAdapter implements ChainedProxyManager {

	private static final int EVENT_MAX_SIZE = 100;
	private final static Logger log = Logger.getLogger(FilterManager.class);
	private static final int MAX_SIZE = 8388608;

	private Tape tape;
	private Recorder recorder;

	public Settings settings;

	private List<Event> events = new ArrayList<Event>();
	private Integer counter = 0;

	public static enum State {
		MOCK, PROXY, RECORD;
	}

	public FilterManager() {
		tape = new Tape();
		settings = new Settings();
		recorder = new Recorder(tape);
	}

	@Override
	public HttpFilters filterRequest(HttpRequest originalRequest) {

		return new HttpFiltersAdapter(originalRequest) {

			Request req;
			Response res;
			StopWatch watch;

			@Override
			public HttpResponse requestPre(HttpObject httpObject) {
				try {
					if (httpObject instanceof DefaultFullHttpRequest) {
						DefaultFullHttpRequest httpFullObj = (DefaultFullHttpRequest) httpObject;
						if (settings.redirect) {
							httpFullObj.headers().set("Host", settings.host + ":" + settings.port);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return null;

			}

			@Override
			public HttpResponse requestPost(HttpObject httpObject) {
				try {
					if (httpObject instanceof DefaultFullHttpRequest) {
						State state = settings.state;

						DefaultFullHttpRequest httpFullObj = (DefaultFullHttpRequest) httpObject;
						req = new Request(httpFullObj, state);

						log.info("Request incoming: " + req.hashCode());
						watch = new StopWatch();
						watch.start();

						if (state == State.PROXY) {
							// No need
						} else if (state == State.MOCK) {
							HttpResponse response = sendToMock(req, watch);
							if (!settings.redirect) {
								return handleNoRedirect();
							}
							return response;
						} else if (state == State.RECORD) {
							// No Need
						}

						if (!settings.redirect) {
							return handleNoRedirect();
						}

						addEvent(counter, res, req, watch);
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
				Integer status = 505;
				State resState = settings.state;
				Response response = new Response(headers, content, protocol, status, resState);
				this.res = response;
				try {
					watch.stop();
				} catch (IllegalStateException e) {

				}
				addEvent(counter++, res, req, watch);
				return response.getHTTPObject();
			}

			@Override
			public HttpObject responsePost(HttpObject httpObject) {
				try {
					if (httpObject instanceof DefaultFullHttpResponse) {
						State state = settings.state;

						DefaultFullHttpResponse httpFullObj = (DefaultFullHttpResponse) httpObject;
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
							// No need
						} else if (state == State.RECORD) {
							sendToRecorder(res, req);
						}

						addEvent(counter++, res, req, watch);

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

		Event event = new Event(id, req, res, new Date(watch.getStartTime()), duration, settings.state);

		Payload payload = new Payload(Payload.Type.EVENT, event);
		EventSocket.broadcast(payload);
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

		watch.stop();

		if (response != null) {
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
		recorder.add(req, res);
		return null;
	}

	/**
	 * Setter and Getters
	 */

	public void setState(State state) {
		log.info("State set to " + state.toString());
		settings.state = state;
	}

	public Tape getTape() {
		return this.tape;
	}

	public void setTape(Tape tape) {
		if (tape == null) {
			return;
		}
		log.info("Settings Tape " + tape.hashCode());
		this.tape = tape;
		recorder.setTape(tape);
	}

	public void setRedirectInfo(String host, Integer port) {
		settings.host = host;
		settings.port = port;
		log.info("Redirecting set to " + settings.host + ":" + settings.port);
	}

	public void setRedirectState(boolean state) {
		log.info("Redirecting state: " + state);
		settings.redirect = state;
	}

	public Boolean getRedirectState() {
		return settings.redirect;
	}

	public String getHost() {
		return settings.host;
	}

	public Integer getPort() {
		return settings.port;
	}

	public State getState() {
		return settings.state;
	}

	public List<Event> getEvents() {
		List<Event> events = this.events;
		this.events = new ArrayList<Event>();
		return events;
	}

	public void lookupChainedProxies(HttpRequest httpRequest, Queue<ChainedProxy> chainedProxies) {
		chainedProxies.add(new ChainedProxyAdapter() {
			@Override
			public InetSocketAddress getChainedProxyAddress() {
				return new InetSocketAddress(getHost(), getPort());
			}
		});

		if (!getRedirectState()) {
			chainedProxies.add(ChainedProxyAdapter.FALLBACK_TO_DIRECT_CONNECTION);
		}
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
		return settings.toJSON();
	}

	public void parseJSON(String source) {
		Settings settings = Settings.parseJSON(source);

		State proxyState = settings.state;
		Boolean redirectState = settings.redirect;
		String host = settings.host;
		Integer port = settings.port;

	}

	public void setSettings(Settings settings) {
		this.setState(settings.state);
		this.setRedirectInfo(settings.host, settings.port);
		this.setRedirectState(settings.redirect);
	}

}