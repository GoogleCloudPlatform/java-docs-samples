/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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

package com.google.appengine.demos;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
public class DumpServlet extends HttpServlet {

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
      throws ServletException, IOException {

    if (Boolean.parseBoolean(request.getParameter("throw"))) {
      throw new ServletException("Test Exception");
    }

    response.setContentType("text/html");
    response.setStatus(HttpServletResponse.SC_OK);

    PrintWriter out = response.getWriter();

    out.println("<h1>DumpServlet</h1>");
    out.println("<h2>Context Fields:</h2>");
    out.println("<pre>");
    out.printf("serverInfo=%s%n", getServletContext().getServerInfo());
    out.printf("getServletContextName=%s%n", getServletContext().getServletContextName());
    out.printf("virtualServerName=%s%n", getServletContext().getVirtualServerName());
    out.printf("contextPath=%s%n", getServletContext().getContextPath());
    out.printf(
        "version=%d.%d%n",
        getServletContext().getMajorVersion(),
        getServletContext().getMinorVersion());
    out.printf(
        "effectiveVersion=%d.%d%n",
        getServletContext().getEffectiveMajorVersion(),
        getServletContext().getEffectiveMinorVersion());
    out.println("</pre>");
    out.println("<h2>Request Fields:</h2>");
    out.println("<pre>");
    out.printf(
        "remoteHost/Addr:port=%s/%s:%d%n",
        request.getRemoteHost(),
        request.getRemoteAddr(),
        request.getRemotePort());
    out.printf(
        "localName/Addr:port=%s/%s:%d%n",
        request.getLocalName(),
        request.getLocalAddr(),
        request.getLocalPort());
    out.printf(
        "scheme=%s method=%s protocol=%s%n",
        request.getScheme(),
        request.getMethod(),
        request.getProtocol());
    out.printf("serverName:serverPort=%s:%d%n", request.getServerName(), request.getServerPort());
    out.printf("requestURI=%s%n", request.getRequestURI());
    out.printf("requestURL=%s%n", request.getRequestURL().toString());
    out.printf(
        "contextPath|servletPath|pathInfo=%s|%s|%s%n",
        request.getContextPath(),
        request.getServletPath(),
        request.getPathInfo());
    out.printf(
        "session/new=%s/%b%n", request.getSession(true).getId(), request.getSession().isNew());
    out.println("</pre>");
    out.println("<h2>Request Headers:</h2>");
    out.println("<pre>");
    for (String n : Collections.list(request.getHeaderNames())) {
      for (String v : Collections.list(request.getHeaders(n))) {
        out.printf("%s: %s%n", n, v);
      }
    }
    out.println("</pre>");
    out.println("<h2>Response Fields:</h2>");
    out.println("<pre>");
    out.printf("bufferSize=%d%n", response.getBufferSize());
    out.printf("encodedURL(\"/foo/bar\")=%s%n", response.encodeURL("/foo/bar"));
    out.printf("encodedRedirectURL(\"/foo/bar\")=%s%n", response.encodeRedirectURL("/foo/bar"));
    out.println("</pre>");
  }
}
