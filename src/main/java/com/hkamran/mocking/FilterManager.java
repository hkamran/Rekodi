package com.hkamran.mocking;

import java.net.InetSocketAddress;
import java.util.Queue;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

import org.apache.log4j.Logger;
import org.littleshoot.proxy.ChainedProxy;
import org.littleshoot.proxy.ChainedProxyAdapter;
import org.littleshoot.proxy.ChainedProxyManager;
import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;

import com.hkamran.mocking.gui.UIEvent;

public class FilterManager extends HttpFiltersSourceAdapter implements ChainedProxyManager {

	private final static Logger log = Logger.getLogger(FilterManager.class);
	private static final int MAX_SIZE = 8388608;
	
	private State state = State.PROXY;
	private Tape tape;
	private Recorder recorder;
	private Debugger debugger;
	
	public String redirectHost;
	public Integer redirectPort;
	public Boolean redirectState = false;	
	
	public static UIEvent event;

	public enum State {
		MOCK, PROXY, RECORD;
	}

	public FilterManager() {
		debugger = new Debugger();
		tape = new Tape();
		recorder = new Recorder(tape);
	}

	@Override
	public HttpFilters filterRequest(HttpRequest originalRequest) {

		return new HttpFiltersAdapter(originalRequest) {

			Request req;
			Response res;
			
			@Override
			public HttpResponse requestPre(HttpObject httpObject) {
				try {
					if (httpObject instanceof DefaultFullHttpRequest) {
						DefaultFullHttpRequest httpFullObj = (DefaultFullHttpRequest) httpObject;
						if (redirectState) {
							httpFullObj.headers().set("Host", redirectHost + ":" + redirectPort);
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
						DefaultFullHttpRequest httpFullObj = (DefaultFullHttpRequest) httpObject;
						req = new Request(httpFullObj);
						
						log.info("Request incoming: " + req.hashCode());
						
						if (state == State.PROXY) {
							//insert code for proxy management
						} else if (state == State.MOCK) {
							return requestToMock(req);
						} else if (state == State.RECORD) {
							return requestToRecord(req);

						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}

				return null;
			}

			@Override
			public HttpObject responsePost(HttpObject httpObject) {
				try {
					if (httpObject instanceof DefaultFullHttpResponse) {
						DefaultFullHttpResponse httpFullObj = (DefaultFullHttpResponse) httpObject;
						res = new Response(httpFullObj);

						updateConsole(req, res);
						log.info("Response outgoing: " + res.hashCode() + " for " + req.hashCode());
						
						if (state == State.PROXY) {

						} else if (state == State.MOCK) {

						} else if (state == State.RECORD) {
							responseToRecorder(res);
						}
						
					}
					
					return httpObject;
				} catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		};
	}
	
	
	public void updateConsole(Request request, Response response) {
		if (event != null) {
			event.event(request, response);
		}
	}

	/**
	 * Request Handlers
	 */
	
	private HttpResponse requestToRecord(Request req) {
		recorder.setCurrentRequest(req);
		return null;
	}

	public HttpResponse requestToMock(final Request request) {
		if (tape == null) {
			return null;
		}

		Response response = tape.getResponse(request);
		
		if (response == null && debugger.getState()) {
			response = debugger.analyze(request);
		}			

		updateConsole(request, response);
	
		if (response != null) {
			log.info("Mocked Response outgoing: " + response.hashCode() + " for " + request.hashCode());
			return (HttpResponse) response.getHttpObject();
		} else {
			log.info("Mocked Response outgoing: null for " + request.hashCode());
			return null;
		}
	}
	
	/**
	 * Response Handlers
	 */
	
	public HttpResponse responseToRecorder(Response res) {
		recorder.save(res);
		return null;
	}
	

	/**
	 * Setter and Getters
	 */

	public void setState(State state) {
		log.info("State set to " + state.toString());
		this.state = state;
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
		this.redirectHost = host;
		this.redirectPort = port;
		log.info("Redirecting set to " + redirectHost + ":" + redirectPort);
	}

	public void setRedirectState(boolean state) {
		log.info("Redirecting state: " + state);
		this.redirectState = state;
	}
	
	public Boolean getRedirectState() {
		return redirectState;
	}

	public String getHost() {
		return redirectHost;
	}
	
	public Integer getPort() {
		return redirectPort;
	}	
	
	public State getState() {
		return state;
	}
	
	public Debugger getDebugger() {
		return debugger;
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

}