package com.hkamran.mocking;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hkamran.mocking.Filter.State;
import com.hkamran.mocking.servers.HTTPServer;
import com.hkamran.mocking.servers.WebSocket;

/**
 * Main entry point
 * 
 * @author HK
 */
@SuppressWarnings("deprecation")
public class Main {

	private static final int DEFAULT_PROXY_PORT = 9090;
	private static final int DEFAULT_WEB_PORT = 8090;
	
	private final static Logger log = LogManager.getLogger(Main.class);

	
	public static void main(String[] args) {
		try {
			
			/**
			 * Read command line arguments
			 */
			
			Options options = createCMDOptions();
			HelpFormatter f = new HelpFormatter();
			CommandLineParser parser = new BasicParser();
			CommandLine cmd;
			f.setWidth(250);
			
			
			cmd = parser.parse(options, args, true);
			Integer webPort = getWebPort(cmd);
			Integer initialProxy = getProxyPort(cmd);
			
			/**
			 * Setup Filter ..
			 * 
			 */
			
			log.info("Starting up Recorder....");
			Proxies proxies = new Proxies();
			
			Integer id = proxies.add("Default Service", initialProxy);
			Proxy proxy = proxies.get(id);
			Filter filter = proxy.getFilter();
			
			filter.setState(State.PROXY);
			filter.setRedirectInfo("Enter Host", 80);
			filter.setRedirectState(true);
			
			
			/**
			 * Start Program
			 */
			
			WebSocket.setProxyManager(proxies);
			HTTPServer.setProxyManager(proxies);
			
			HTTPServer frontEnd = new HTTPServer();
			
			log.info("Startup finished! ");
			frontEnd.start(webPort);
	
		} catch (Exception e) {
			throw new RuntimeException("Unable to start application...", e);
		}
	}

	private static Integer getProxyPort(CommandLine cmd) {
		Integer webPort = DEFAULT_PROXY_PORT;
		if (cmd.hasOption("proxyPort")) {
			webPort = Integer.parseInt(cmd.getOptionValue("proxyPort"));
		}
		return webPort;
	}
	
	private static Integer getWebPort(CommandLine cmd) {
		Integer webPort = DEFAULT_WEB_PORT;
		if (cmd.hasOption("webPort")) {
			webPort = Integer.parseInt(cmd.getOptionValue("webPort"));
		}
		return webPort;
	}
	
	private static Options createCMDOptions() {
		Option webPort = new Option("webPort", true,
				"Port for the web gui");
		webPort.setArgName("webPort");
		webPort.setRequired(false);
		
		Option proxyPort = new Option("proxyPort", true,
				"Port for the initial proxy");
		proxyPort.setArgName("proxyPort");
		proxyPort.setRequired(false);

		Options options = new Options();
		options.addOption(webPort);
		options.addOption(proxyPort);
		return options;
}
}
