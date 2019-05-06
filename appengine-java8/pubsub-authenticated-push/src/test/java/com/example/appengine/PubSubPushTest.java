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

package com.example.appengine;

import static org.mockito.Mockito.when;

import com.example.appengine.pubsub.Data;
import com.example.appengine.pubsub.PubSubPush;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.impl.TextCodec;
import java.util.Date;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link PubSubPush}. */
@RunWith(JUnit4.class)
public class PubSubPushTest {
  @Mock private HttpServletRequest mockRequest;
  @Mock private HttpServletResponse mockResponse;

  private PubSubPush pubSubPushServlet;

  private String PRIVATE_KEY_BYTES;

  private ServletContext servletContext;

  private ServletConfig servletConfig;

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    pubSubPushServlet = new PubSubPush();
    PRIVATE_KEY_BYTES = PubSubPush.readFile("privatekey.pem");
    servletConfig = Mockito.mock(ServletConfig.class);
    servletContext = Mockito.mock(ServletContext.class);
    pubSubPushServlet.init(servletConfig);
    servletContext.setAttribute("data", new Data());
  }

  @Test
  public void testPost() throws Exception {
    String authorization =
        "Bearer "
            + createJWTToken("https://accounts.google.com", "1234567890", new Date().getTime());
    when(mockRequest.getParameter("token")).thenReturn("1234abc");
    when(mockRequest.getHeader("Authorization")).thenReturn(authorization);
    when(pubSubPushServlet.getServletContext()).thenReturn(servletContext);
    pubSubPushServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = Exception.class)
  public void testPostErrors() throws Exception {
    pubSubPushServlet.doPost(mockRequest, mockResponse);
  }

  @Test(expected = Exception.class)
  public void testPostErrorsWithBadToken() throws Exception {
    when(mockRequest.getParameter("token")).thenReturn("bad");
    pubSubPushServlet.doPost(mockRequest, mockResponse);
  }

  private String createJWTToken(String issuer, String subject, long ttlMillis) {
    long nowMillis = System.currentTimeMillis();
    Date now = new Date(nowMillis);

    JwtBuilder builder =
        Jwts.builder()
            .setIssuedAt(now)
            .setSubject(subject)
            .setIssuer(issuer)
            .signWith(SignatureAlgorithm.HS256, TextCodec.BASE64.decode(PRIVATE_KEY_BYTES));

    if (ttlMillis >= 0) {
      long expMillis = nowMillis + ttlMillis;
      Date exp = new Date(expMillis);
      builder.setExpiration(exp);
    }

    return builder.compact();
  }
}
