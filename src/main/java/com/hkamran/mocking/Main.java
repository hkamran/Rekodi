package com.hkamran.mocking;

import org.apache.log4j.Logger;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.extras.SelfSignedMitmManager;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;

import com.hkamran.mocking.FilterManager.State;
import com.hkamran.mocking.gui.MainPage;
import com.hkamran.mocking.gui.UIEvent;
import com.hkamran.mocking.servers.BackEndServer;
import com.hkamran.mocking.servers.FrontEndServer;

/**
 * Hello world!
 *
 */
public class Main {

	private final static Logger log = Logger.getLogger(Main.class);

	public static void main(String[] args) throws InterruptedException {
		
		/**
		 * Setup Filter
		 */
		
		FilterManager filter = new FilterManager();
		Integer port = 80;
		String host = "www.thomas-bayer.com";
		
		filter.setState(State.PROXY);
		filter.setRedirectInfo(host, port);
		filter.setRedirectState(false);
		
		
		final MainPage window = new MainPage(filter);
		
		FilterManager.event = new UIEvent() {
			public void event(Object... objs) {
				Object obj1 = objs[0];
				Object obj2 = objs[1];
				Object obj3 = objs[2];
				if (obj1 instanceof Request && obj2 instanceof Response) {
					window.updateConsole((Request) obj1, (Response) obj2, (Long) obj3);
				} else if (obj1 instanceof String) {
					window.updateConsole((String) obj1);
				}
			}
		};
		Tape.setUIEventHandler(new UIEvent() {

			public void event(Object... objs) {
				Object obj = objs[0];
				if (obj instanceof Tape) {
					window.updateTree((Tape) obj);
				}
			}
		});						

		Debugger.setUIEventHandler(new UIEvent() {
			public void event(Object... objs) {
				Object obj = objs[0];
				if (obj instanceof Debugger) {
					window.debugger((Debugger) obj);
				}
			}
		});	
		
		BackEndServer.setFilter("default", filter);
		
		
		/**
		 * Start Program
		 */
		
		Integer proxyPort = 9090;

		log.info("Starting Service Recorder at port " + proxyPort);
		HttpProxyServer proxy = DefaultHttpProxyServer.bootstrap().withPort(proxyPort).withFiltersSource(filter).withManInTheMiddle(new SelfSignedMitmManager()).start();
		BackEndServer.start(7090);
		FrontEndServer.start(8090);
		window.open();
		proxy.stop();
	}
}
