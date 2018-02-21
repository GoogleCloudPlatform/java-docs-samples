/*
 * Copyright 2016 Google Inc.
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

package com.example.appengine.firetactoe;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalURLFetchServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableMap;
import com.googlecode.objectify.Objectify;
import com.googlecode.objectify.ObjectifyFactory;
import com.googlecode.objectify.ObjectifyService;
import com.googlecode.objectify.util.Closeable;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

/**
 * Unit tests for {@link MoveServlet}.
 */
@RunWith(JUnit4.class)
public class MoveServletTest {
  private static final String USER_EMAIL = "whisky@tangofoxtr.ot";
  private static final String USER_ID = "whiskytangofoxtrot";
  private static final String FIREBASE_DB_URL = "http://firebase.com/dburl";

  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
          // Set no eventual consistency, that way queries return all results.
          // http://g.co/cloud/appengine/docs/java/tools/localunittesting#Java_Writing_High_Replication_Datastore_tests
          new LocalDatastoreServiceTestConfig().setDefaultHighRepJobPolicyUnappliedJobPercentage(0),
          new LocalUserServiceTestConfig(),
          new LocalURLFetchServiceTestConfig()
          )
      .setEnvEmail(USER_EMAIL)
      .setEnvAuthDomain("gmail.com")
      .setEnvAttributes(new HashMap(
          ImmutableMap.of("com.google.appengine.api.users.UserService.user_id_key", USER_ID)));

  @Mock private HttpServletRequest mockRequest;
  @Mock private HttpServletResponse mockResponse;
  protected Closeable dbSession;

  private MoveServlet servletUnderTest;

  @BeforeClass
  public static void setUpBeforeClass() {
    // Reset the Factory so that all translators work properly.
    ObjectifyService.setFactory(new ObjectifyFactory());
    ObjectifyService.register(Game.class);
    // Mock out the firebase config
    FirebaseChannel.firebaseConfigStream = new ByteArrayInputStream(
        String.format("databaseURL: \"%s\"", FIREBASE_DB_URL).getBytes());
  }

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    helper.setUp();
    dbSession = ObjectifyService.begin();

    servletUnderTest = spy(new MoveServlet());

    helper.setEnvIsLoggedIn(true);
    // Make sure there are no firebase requests if we don't expect it
    FirebaseChannel.getInstance(null).httpTransport = null;
  }

  @After
  public void tearDown() {
    dbSession.close();
    helper.tearDown();
  }

  @Test
  public void doPost_myTurn_move() throws Exception {
    // Insert a game
    Objectify ofy = ObjectifyService.ofy();
    Game game = new Game(USER_ID, "my-opponent", "         ", true);
    ofy.save().entity(game).now();
    String gameKey = game.getId();

    when(mockRequest.getParameter("gameKey")).thenReturn(gameKey);
    when(mockRequest.getParameter("cell")).thenReturn("1");

    // Mock out the firebase response. See
    // http://g.co/dv/api-client-library/java/google-http-java-client/unit-testing
    MockHttpTransport mockHttpTransport = spy(new MockHttpTransport() {
      @Override
      public LowLevelHttpRequest buildRequest(String method, String url) throws IOException {
        return new MockLowLevelHttpRequest() {
          @Override
          public LowLevelHttpResponse execute() throws IOException {
            MockLowLevelHttpResponse response = new MockLowLevelHttpResponse();
            response.setStatusCode(200);
            return response;
          }
        };
      }
    });
    FirebaseChannel.getInstance(null).httpTransport = mockHttpTransport;

    Mockito.doReturn(null).when(servletUnderTest).getServletContext();
    servletUnderTest.doPost(mockRequest, mockResponse);

    game = ofy.load().type(Game.class).id(gameKey).safe();
    assertEquals(game.board, " X       ");

    verify(mockHttpTransport, times(2)).buildRequest(
        eq("PATCH"), Matchers.matches(FIREBASE_DB_URL + "/channels/[\\w-]+.json$"));
  }

  @Ignore
  public void doPost_notMyTurn_move() throws Exception {
    // Insert a game
    Objectify ofy = ObjectifyService.ofy();
    Game game = new Game(USER_ID, "my-opponent", "         ", false);
    ofy.save().entity(game).now();
    String gameKey = game.getId();

    when(mockRequest.getParameter("gameKey")).thenReturn(gameKey);
    when(mockRequest.getParameter("cell")).thenReturn("1");

    servletUnderTest.doPost(mockRequest, mockResponse);

    verify(mockResponse).sendError(401);
  }
}
