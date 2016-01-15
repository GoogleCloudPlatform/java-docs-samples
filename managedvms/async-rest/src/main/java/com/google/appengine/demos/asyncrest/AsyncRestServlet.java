package com.google.appengine.demos.asyncrest;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

import javax.servlet.AsyncContext;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.eclipse.jetty.client.HttpClient;
import org.eclipse.jetty.client.api.Response;
import org.eclipse.jetty.client.api.Result;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.util.BufferUtil;
import org.eclipse.jetty.util.Utf8StringBuilder;
import org.eclipse.jetty.util.ajax.JSON;
import org.eclipse.jetty.util.ssl.SslContextFactory;

/**
 * AsyncRestServlet. May be configured with init parameters:
 * <dl>
 * <dt>appid</dt>
 * <dd>The Google app key to use</dd>
 * </dl>
 *
 */
public class AsyncRestServlet extends AbstractRestServlet {
    final static String RESULTS_ATTR = "com.google.appengine.demos.asyncrest.client";
    final static String DURATION_ATTR = "com.google.appengine.demos.asyncrest.duration";
    final static String START_ATTR = "com.google.appengine.demos.asyncrest.start";

    HttpClient client;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException {
	super.init(servletConfig);
	SslContextFactory sslContextFactory = new SslContextFactory();
	client = new HttpClient(sslContextFactory);

	try {
	    client.start();
	} catch (Exception e) {
	    throw new ServletException(e);
	}
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {

	if (key == null) {
	    response.sendError(500, APPKEY + " not set");
	    return;
	}

	Long start = System.nanoTime();

	// Do we have results yet?
	Queue<Map<String, Object>> results = (Queue<Map<String, Object>>) request.getAttribute(RESULTS_ATTR);

	// If no results, this must be the first dispatch, so send the REST
	// request(s)
	if (results == null) {
	    // define results data structures
	    final Queue<Map<String, Object>> resultsQueue = new ConcurrentLinkedQueue<>();
	    request.setAttribute(RESULTS_ATTR, results = resultsQueue);

	    // suspend the request
	    // This is done before scheduling async handling to avoid race of
	    // dispatch before startAsync!
	    final AsyncContext async = request.startAsync();
	    async.setTimeout(30000);

	    // extract keywords to search for
	    String lat = sanitize(request.getParameter(LATITUDE_PARAM));
	    String longitude = sanitize(request.getParameter(LONGITUDE_PARAM));
	    String radius = sanitize(request.getParameter(RADIUS_PARAM));
	    String[] keywords = sanitize(request.getParameter(ITEMS_PARAM)).split(",");

	    final AtomicInteger outstanding = new AtomicInteger(keywords.length);

	    // Send request each keyword
	    for (final String item : keywords) {
		client.newRequest(restQuery(lat + "," + longitude, radius, item)).method(HttpMethod.GET)
			.send(new AsyncRestRequest() {
			    @Override
			    void onLocationFound(Map<String, Object> result) {
				resultsQueue.add(result);
			    }

			    @Override
			    void onComplete() {
				if (outstanding.decrementAndGet() <= 0)
				    async.dispatch();
			    }
			});
	    }

	    // save timing info and return
	    request.setAttribute(START_ATTR, start);
	    request.setAttribute(DURATION_ATTR, System.nanoTime() - start);

	    return;
	}

	// We have results!
	// Generate the response
	String thumbs = generateResults(results);

	response.setContentType("text/html");
	PrintWriter out = response.getWriter();
	out.println("<html><head>");
	out.println(STYLE);
	out.println("</head><body><small>");

	long initial = (Long) request.getAttribute(DURATION_ATTR);
	long start0 = (Long) request.getAttribute(START_ATTR);

	long now = System.nanoTime();
	long total = now - start0;
	long generate = now - start;
	long thread = initial + generate;

	String loc = sanitize(request.getParameter(LOC_PARAM));
	out.print("<b>Asynchronous: Requesting " + sanitize(request.getParameter(ITEMS_PARAM)) + " near "
		+ (loc != null ? loc
			: "lat=" + sanitize(request.getParameter(LATITUDE_PARAM)) + " long="
				+ sanitize(request.getParameter(LONGITUDE_PARAM)))
		+ "</b><br/>");
	out.print("Total Time: " + ms(total) + "ms<br/>");

	out.print("Thread held (<span class='red'>red</span>): " + ms(thread) + "ms (" + ms(initial) + " initial + "
		+ ms(generate) + " generate )<br/>");
	out.print("Async wait (<span class='green'>green</span>): " + ms(total - thread) + "ms<br/>");

	out.println("<img border='0px' src='asyncrest/red.png'   height='20px' width='" + width(initial) + "px'>"
		+ "<img border='0px' src='asyncrest/green.png' height='20px' width='" + width(total - thread) + "px'>"
		+ "<img border='0px' src='asyncrest/red.png'   height='20px' width='" + width(generate) + "px'>");

	out.println("<br/>");
	out.print("First 5 results:<br/>");
	out.println(thumbs);
	out.println("</small>");
	out.println("</body></html>");
	out.close();
    }

    private abstract class AsyncRestRequest extends Response.Listener.Adapter {
	final Utf8StringBuilder _content = new Utf8StringBuilder();

	AsyncRestRequest() {
	}

	@Override
	public void onContent(Response response, ByteBuffer content) {
	    byte[] bytes = BufferUtil.toArray(content);
	    _content.append(bytes, 0, bytes.length);
	}

	@Override
	public void onComplete(Result result) {
	    // extract results
	    Map<String, Object> data = (Map<String, Object>) JSON.parse(_content.toString());
	    if (data != null) {
		Object[] results = (Object[]) data.get("results");
		if (results != null) {
		    for (Object o : results)
			onLocationFound((Map<String, Object>) o);
		}
	    }
	    onComplete();

	}

	abstract void onLocationFound(Map<String, Object> details);

	abstract void onComplete();

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
	    throws ServletException, IOException {
	doGet(request, response);
    }
}
