/**
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

package com.example.appengine.mail;

import javax.mail.internet.MimeMessage;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Base class for handling the filtering of incoming emails in App Engine.
 */
// [START example]
public abstract class MailHandlerBase implements Filter {

  private Pattern pattern = null;

  protected MailHandlerBase(String pattern) {
    if (pattern == null || pattern.trim().length() == 0)
    {
      throw new IllegalArgumentException("Expected non-empty regular expression");
    }
    this.pattern = Pattern.compile("/_ah/mail/"+pattern);
  }

  @Override public void init(FilterConfig config) throws ServletException { }

  @Override public void destroy() { }

  /**
   * Process the message. A message will only be passed to this method
   * if the servletPath of the message (typically the recipient for
   * appengine) satisfies the pattern passed to the constructor. If
   * the implementation returns false, control is passed
   * to the next filter in the chain. If the implementation returns
   * true, the filter chain is terminated.
   *
   * The Matcher for the pattern can be retrieved via
   * getMatcherFromRequest (e.g. if groups are used in the pattern).
   */
  protected abstract boolean processMessage(HttpServletRequest req, HttpServletResponse res) throws ServletException;

  @Override
  public void doFilter(ServletRequest sreq, ServletResponse sres, FilterChain chain)
      throws IOException, ServletException {

    HttpServletRequest req = (HttpServletRequest) sreq;
    HttpServletResponse res = (HttpServletResponse) sres;

    MimeMessage message = getMessageFromRequest(req);
    Matcher m = applyPattern(req);

    if (m != null && processMessage(req, res)) {
      return;
    }

    chain.doFilter(req, res); // Try the next one

  }

  private Matcher applyPattern(HttpServletRequest req) {
    Matcher m = pattern.matcher(req.getServletPath());
    if (!m.matches()) m = null;

    req.setAttribute("matcher", m);
    return m;
  }

  protected Matcher getMatcherFromRequest(ServletRequest req) {
    return (Matcher) req.getAttribute("matcher");
  }

  protected MimeMessage getMessageFromRequest(ServletRequest req) throws ServletException {
    MimeMessage message = (MimeMessage) req.getAttribute("mimeMessage");
    if (message == null) {
      try {
        Properties props = new Properties();
        Session session = Session.getDefaultInstance(props, null);
        message = new MimeMessage(session, req.getInputStream());
        req.setAttribute("mimeMessage", message);

      } catch (MessagingException e) {
        throw new ServletException("Error processing inbound message", e);
      } catch (IOException e) {
        throw new ServletException("Error processing inbound message", e);
      }
    }
    return message;
  }
}
// [END example]
