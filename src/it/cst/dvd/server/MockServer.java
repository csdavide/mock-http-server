package it.cst.dvd.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.text.MessageFormat;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javaxt.http.servlet.HttpServlet;
import javaxt.http.servlet.HttpServletRequest;
import javaxt.http.servlet.HttpServletResponse;
import javaxt.http.servlet.ServletException;
import javaxt.http.servlet.ServletInputStream;

public class MockServer {
	static final String XML_ENVELOPE = "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">\n<soap:Body>\n{0}\n</soap:Body>\n</soap:Envelope>";
	static final String XML_FAULT = "<soap:Fault>\n<faultcode>soap:Server</faultcode>\n<faultstring>{0}</faultstring>\n</soap:Fault>";

	private static String path;

	public static void main(final String[] args) {
		try {
			int port = 9080;
			try {
				port = Integer.parseInt(args[0]);
			} catch (final Exception e) {
			}
			path = args[1];
			System.out.println(args[0] + " # " + args[1]);
			final int numThreads = 100;
			final javaxt.http.Server server = new javaxt.http.Server(port,
					numThreads,
					new HttpServer());
			server.start();
		} catch (final Exception e) {
			System.out.println("Server could not start - error : "  + e.toString());
			System.exit(1);
		}
	}

	// Custom Servlet
	private static class HttpServer extends HttpServlet {
		@Override
		public	void
				processRequest(	final HttpServletRequest request,
								final HttpServletResponse response)	throws ServletException,
																	java.io.IOException {
			String data = "";
			try {
				final String requestURI = request.getRequestURI();

				if (requestURI.indexOf("/service/soap") != -1) {
					response.setContentType("text/xml");
					NodeList nodes = getRequestBody(request);
					if (nodes != null && nodes.getLength() > 0) {
						Node node = nodes.item(0);
						String sname = node.getLocalName();
						// String sname = IOUtils.toString(streamIn);
						final String sdata = path + "/" + sname + ".xml";
						final File file = new File(sdata);
						if (file.exists()) {
							final InputStream rd = new FileInputStream(sdata);
							final String xml = IOUtils.toString(rd);
							final String env = MessageFormat
									.format(XML_ENVELOPE, xml);
							data = new String(env);
							response.write(data);
							return;
						}
						response.write(makeFault("NoService " + sname));
						return;
					}
				} else if (requestURI.indexOf("/service/rest") != -1) {
					response.setContentType("application/json");
					final String sname = request.getParameter("method");
					if (sname != null) {
						final String sdata = path + "/" + sname + ".xml";
						final File file = new File(sdata);
						if (file.exists()) {
							final InputStream rd = new FileInputStream(sdata);
							final String xml = IOUtils.toString(rd);
							final String env = MessageFormat
									.format(XML_ENVELOPE, xml);
							data = new String(env);
							response.write(data);
							return;
						}
						response.write(makeFault("NoService " + sname));
						return;
					}
				}
				response.write(makeFault("InvalidRequest"));
			} catch (final Exception e) {
				e.printStackTrace();
				response.write(makeFault("ServerError"));
			}
		}

		private String makeFault(String msg) {
			String fault = MessageFormat.format(XML_FAULT, msg);
			String env = MessageFormat.format(XML_ENVELOPE, fault);
			return env;
		}

		private	NodeList
				getRequestBody(final HttpServletRequest request) throws Exception {
			ServletInputStream streamIn = request.getInputStream();
			DocumentBuilderFactory builderFactory = DocumentBuilderFactory
					.newInstance();
			builderFactory.setNamespaceAware(true);
			DocumentBuilder builder = builderFactory.newDocumentBuilder();
			Document xmlDocument = builder.parse(streamIn);
			XPath xPath = XPathFactory.newInstance().newXPath();
			XPathExpression xexpr = xPath
					.compile("//*[local-name()='Envelope']/*[local-name()='Body']/*");
			NodeList nodes = (NodeList) xexpr.evaluate(	xmlDocument,
														XPathConstants.NODESET);
			return nodes;
		}
	}
}
