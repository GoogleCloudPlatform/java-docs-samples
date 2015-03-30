package mysite.server;

import java.io.IOException;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

public class LogFilterImpl implements Filter {

	private FilterConfig filterConfig;
	private static final Logger log = Logger.getLogger(LogFilterImpl.class.getName());

	public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain)
		throws IOException, ServletException {
		log.warning("Log filter processed a " + getFilterConfig().getInitParameter("logType")
				+ " request");

		filterChain.doFilter(request, response);
	}

	public FilterConfig getFilterConfig() {
		return filterConfig;
	}

	public void init(FilterConfig filterConfig) {
		this.filterConfig = filterConfig;
	}

	public void destroy() {}

}
