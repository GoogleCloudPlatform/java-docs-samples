package com.google.appengine.demos.asyncrest;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Abstract Servlet implementation class AsyncRESTServlet.
 * Enquires ebay REST service for auctions by key word.
 * May be configured with init parameters: <dl>
 * <dt>appid</dt><dd>The eBay application ID to use</dd>
 * </dl>
 * Each request examines the following request parameters:<dl>
 * <dt>items</dt><dd>The keyword to search for</dd>
 * </dl>
 */
public class AbstractRestServlet extends HttpServlet
{
	protected final static int MAX_RESULTS = 5;
	protected final static String GOOGLE_API_KEY_NAME = "async_example_place_key";
	protected final static String GOOGLE_API_KEY = "AIzaSyABCgUB4wc290F9LcIigNXurT6yNV92yfY";

    protected final static String STYLE = 
        "<style type='text/css'>"+
        "  img.thumb:hover {height:50px}"+
        "  img.thumb {vertical-align:text-top}"+
        "  span.red {color: #ff0000}"+
        "  span.green {color: #00ff00}"+
        "  iframe {border: 0px}"+
        "</style>";


    protected final static String APPID_PARAM = "appid";
    protected final static String LOC_PARAM = "loc";
    protected final static String ITEMS_PARAM = "items";
    protected final static String LATITUDE_PARAM = "lat";
    protected final static String LONGITUDE_PARAM = "long";
    protected final static String RADIUS_PARAM ="radius";
    protected String _key;

    @Override
    public void init(ServletConfig servletConfig) throws ServletException
    {
        if (servletConfig.getInitParameter(APPID_PARAM) == null)
            _key = GOOGLE_API_KEY;
        else
            _key = servletConfig.getInitParameter(APPID_PARAM);
    }


    public static String sanitize(String s)
    {
        if (s==null)
            return null;
        return s.replace("<","?").replace("&","?").replace("\n","?");
    }
    
    protected String restQuery (String coordinates, String radius, String item)
    {
    	try
    	{
    		return "https://maps.googleapis.com/maps/api/place/nearbysearch/json?key="+_key
    				+"&location="+URLEncoder.encode(coordinates,"UTF-8")
    				+"&types="+URLEncoder.encode(item,"UTF-8")
    				+"&radius="+URLEncoder.encode(radius, "UTF-8");
    		
    	}
    	catch (Exception e)
    	{
    		throw new RuntimeException(e);
    	}
    }
    
   
    
    public String generateResults (Queue<Map<String,Object>> results)
    {   
    	StringBuilder thumbs = new StringBuilder();
    	int resultCount = 0;
    	Iterator<Map<String,Object>> itor = results.iterator();
   
        while (resultCount < MAX_RESULTS && itor.hasNext())
        {
            Map m = (Map)itor.next();
            String name = (String)m.get("name");  
            Object[] photos = (Object[])m.get("photos");
            if (photos != null && photos.length > 0)
            {
            	resultCount++;
            	thumbs.append("<img class='thumb' border='1px' height='40px'"+
            			" src='"+getPhotoURL((String)(((Map)photos[0]).get("photo_reference")))+"'"+
            			" title='"+name+"'"+
            			"/>");
            	thumbs.append("</a>&nbsp;");
            }
        }
        return thumbs.toString();
    }
    
    public String getPhotoURL (String photoref)
    {
    	return "https://maps.googleapis.com/maps/api/place/photo?key="+_key
				+"&photoreference="+photoref
				+"&maxheight=40";
    }
   

    protected String ms(long nano)
    {
        BigDecimal dec = new BigDecimal(nano);
        return dec.divide(new BigDecimal(1000000L)).setScale(1,RoundingMode.UP).toString();
    }
    
    protected int width(long nano)
    {
        int w=(int)((nano+999999L)/5000000L);
        if (w==0)
            w=2;
        return w;
    }
    
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        doGet(request, response);
    }

}
