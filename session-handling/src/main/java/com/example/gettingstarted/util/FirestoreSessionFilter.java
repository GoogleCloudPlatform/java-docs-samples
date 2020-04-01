/* Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.gettingstarted.util;

import com.example.gettingstarted.actions.HelloWorldServlet;
import com.google.cloud.firestore.CollectionReference;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebFilter(
    filterName = "FirestoreSessionFilter ",
    urlPatterns = {""})
public class FirestoreSessionFilter implements Filter {
  private static final SimpleDateFormat dtf = new SimpleDateFormat("yyyyMMddHHmmssSSS");
  private static final Logger logger = Logger.getLogger(HelloWorldServlet.class.getName());
  private static Firestore firestore;
  private static CollectionReference sessions;

  // [START sessions_handling_init]
  @Override
  public void init(FilterConfig config) throws ServletException {
    // Initialize local copy of datastore session variables.
    firestore = FirestoreOptions.getDefaultInstance().getService();
    sessions = firestore.collection("sessions");

    try {
      // Delete all sessions unmodified for over two days.
      Calendar cal = Calendar.getInstance();
      cal.setTime(new Date());
      cal.add(Calendar.HOUR, -48);
      Date twoDaysAgo = Calendar.getInstance().getTime();
      QuerySnapshot sessionDocs =
          sessions.whereLessThan("lastModified", dtf.format(twoDaysAgo)).get().get();
      for (QueryDocumentSnapshot snapshot : sessionDocs.getDocuments()) {
        snapshot.getReference().delete();
      }
    } catch (InterruptedException | ExecutionException e) {
      throw new ServletException("Exception initializing FirestoreSessionFilter.", e);
    }
  }
  // [END sessions_handling_init]

  // [START sessions_handling_filter]
  @Override
  public void doFilter(ServletRequest servletReq, ServletResponse servletResp, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest req = (HttpServletRequest) servletReq;
    HttpServletResponse resp = (HttpServletResponse) servletResp;

    // For this app only call Firestore for requests to base path `/`.
    if (!req.getServletPath().equals("/")) {
      chain.doFilter(servletReq, servletResp);
      return;
    }

    // Check if the session cookie is there, if not there, make a session cookie using a unique
    // identifier.
    String sessionId = getCookieValue(req, "bookshelfSessionId");
    if (sessionId.equals("")) {
      String sessionNum = new BigInteger(130, new SecureRandom()).toString(32);
      Cookie session = new Cookie("bookshelfSessionId", sessionNum);
      session.setPath("/");
      resp.addCookie(session);
    }

    // session variables for request
    Map<String, Object> firestoreMap = null;
    try {
      firestoreMap = loadSessionVariables(req);
    } catch (ExecutionException | InterruptedException e) {
      throw new ServletException("Exception loading session variables.", e);
    }

    for (Map.Entry<String, Object> entry : firestoreMap.entrySet()) {
      servletReq.setAttribute(entry.getKey(), entry.getValue());
    }

    // Allow the servlet to process request and response
    chain.doFilter(servletReq, servletResp);

    // Create session map
    HttpSession session = req.getSession();
    Map<String, Object> sessionMap = new HashMap<>();
    Enumeration<String> attrNames = session.getAttributeNames();
    while (attrNames.hasMoreElements()) {
      String attrName = attrNames.nextElement();
      sessionMap.put(attrName, session.getAttribute(attrName));
    }

    logger.info("Saving data to " + sessionId + " with views: " + session.getAttribute("views"));
    firestore.runTransaction((ob) -> sessions.document(sessionId).set(sessionMap));
  }
  // [END sessions_handling_filter]

  private String getCookieValue(HttpServletRequest req, String cookieName) {
    Cookie[] cookies = req.getCookies();
    if (cookies != null) {
      for (Cookie cookie : cookies) {
        if (cookie.getName().equals(cookieName)) {
          return cookie.getValue();
        }
      }
    }
    return "";
  }

  // [START sessions_load_session_variables]

  /**
   * Take an HttpServletRequest, and copy all of the current session variables over to it
   *
   * @param req Request from which to extract session.
   * @return a map of strings containing all the session variables loaded or an empty map.
   */
  private Map<String, Object> loadSessionVariables(HttpServletRequest req)
      throws ExecutionException, InterruptedException {
    Map<String, Object> datastoreMap = new HashMap<>();
    String sessionId = getCookieValue(req, "bookshelfSessionId");
    if (sessionId.equals("")) {
      return datastoreMap;
    }

    return firestore
        .runTransaction(
            (ob) -> {
              DocumentSnapshot session = sessions.document(sessionId).get().get();
              Map<String, Object> data = session.getData();
              if (data == null) {
                data = Maps.newHashMap();
              }
              return data;
            })
        .get();
  }
  // [END sessions_load_session_variables]
}
