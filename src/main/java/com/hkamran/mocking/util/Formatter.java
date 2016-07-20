package com.hkamran.mocking.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.apache.xml.serialize.OutputFormat;
import org.apache.xml.serialize.XMLSerializer;
import org.json.JSONArray;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class Formatter {

	private final static Logger log = LogManager.getLogger(Formatter.class);

	public static String format(String content) {

		if (content == null) {
			log.warn("Formating a null content");
			return "";
		}

		if (content.length() == 0) {
			return content;
		}

		if (isXML(content)) {
			return prettifyXML(content);
		} else if (isJSON(content)) {
			return prettyifyJSON(content);
		} else {
			log.error("Unable to format unknown content type");
		}
		return content;

	}

	private static String prettyifyJSON(String content) {
		try {
			JSONObject json = new JSONObject(content);
			return json.toString(2);
		} catch (JSONException e) {
			log.error("Unable to format JSON content");
			return content;
		}

	}

	private static String prettifyXML(String content) throws TransformerFactoryConfigurationError {
		try {

			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(false);
			dbf.setValidating(false);
			dbf.setFeature("http://xml.org/sax/features/namespaces", false);
			dbf.setFeature("http://xml.org/sax/features/validation", false);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

			DocumentBuilder db = dbf.newDocumentBuilder();
			InputSource is = new InputSource(new StringReader(content));

			final Document document = db.parse(is);
			OutputFormat format = new OutputFormat(document);
			format.setLineWidth(100);
			format.setIndenting(true);
			format.setIndent(3);

			Writer out = new StringWriter();
			XMLSerializer serializer = new XMLSerializer(out, format);
			serializer.serialize(document);

			return out.toString();
		} catch (Exception e) {
			log.error("Unable to format xml content");
		}
		return content;
	}

	private static Boolean isXML(String content) {
		if (content.startsWith("<")) {
			return true;
		}
		return false;
	}

	private static Boolean isJSON(String content) {
		try {
			new JSONObject(content);
			return true;
		} catch (JSONException ex) {
			try {
				new JSONArray(content);
				return true;
			} catch (JSONException ex1) {

			}
		}
		return false;
	}
}
