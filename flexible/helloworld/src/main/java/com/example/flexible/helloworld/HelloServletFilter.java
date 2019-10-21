/*
 * Copyright 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.flexible.helloworld;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import com.google.cloud.logging.TraceLoggingEnhancer;
import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@WebFilter(filterName = "HelloServletFilter")
public class HelloServletFilter implements Filter {

  private final Logger logger = LoggerFactory.getLogger(HelloServletFilter.class);

  /**
   * JoranConfigurator is used to parse the logback.xml configuration file to use logback Appender
   * for logging.
   *
   * @param filterConfig configuration for servlet filter
   */
  @Override
  public void init(FilterConfig filterConfig) {
    try {
      LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
      final ServletContext servletContext = filterConfig.getServletContext();
      JoranConfigurator jc = new JoranConfigurator();
      jc.setContext(context);
      context.reset();
      // set application-name will be used while logging.
      context.putProperty("application-name", "hello world");
      jc.doConfigure(servletContext.getRealPath("/WEB-INF/logback.xml"));
    } catch (Exception e) {
      logger.error(e.getMessage());
    }
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) request;
    // setting current traceId
    TraceLoggingEnhancer.setCurrentTraceId(req.getRemoteHost() + " for " + req.getRequestURL());
    /*logger.info(TraceLoggingEnhancer.getCurrentTraceId());*/
    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {}
}
