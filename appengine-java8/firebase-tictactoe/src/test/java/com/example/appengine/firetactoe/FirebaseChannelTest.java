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

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.testing.http.MockHttpTransport;
import com.google.api.client.testing.http.MockLowLevelHttpRequest;
import com.google.api.client.testing.http.MockLowLevelHttpResponse;
import com.google.appengine.tools.development.testing.LocalAppIdentityServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link FirebaseChannel}. */
@RunWith(JUnit4.class)
public class FirebaseChannelTest {
  private static final String FIREBASE_DB_URL = "http://firebase.com/dburl";
  private final LocalServiceTestHelper helper =
      new LocalServiceTestHelper(new LocalAppIdentityServiceTestConfig());

  private static FirebaseChannel firebaseChannel;

  @BeforeClass
  public static void setUpBeforeClass() {
    // Mock out the firebase config
    FirebaseChannel.firebaseConfigStream =
        new ByteArrayInputStream(String.format("databaseURL: \"%s\"", FIREBASE_DB_URL).getBytes());

    firebaseChannel = FirebaseChannel.getInstance();
  }

  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void sendFirebaseMessage_create() throws Exception {
    // Mock out the firebase response. See
    // http://g.co/dv/api-client-library/java/google-http-java-client/unit-testing
    MockHttpTransport mockHttpTransport =
        spy(
            new MockHttpTransport() {
              @Override
              public LowLevelHttpRequest buildRequest(String method, String url)
                  throws IOException {
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
    FirebaseChannel.getInstance().httpTransport = mockHttpTransport;

    firebaseChannel.sendFirebaseMessage("my_key", new Game());

    verify(mockHttpTransport, times(1))
        .buildRequest("PATCH", FIREBASE_DB_URL + "/channels/my_key.json");
  }

  @Test
  public void sendFirebaseMessage_delete() throws Exception {
    // Mock out the firebase response. See
    // http://g.co/dv/api-client-library/java/google-http-java-client/unit-testing
    MockHttpTransport mockHttpTransport =
        spy(
            new MockHttpTransport() {
              @Override
              public LowLevelHttpRequest buildRequest(String method, String url)
                  throws IOException {
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
    FirebaseChannel.getInstance().httpTransport = mockHttpTransport;

    firebaseChannel.sendFirebaseMessage("my_key", null);

    verify(mockHttpTransport, times(1))
        .buildRequest("DELETE", FIREBASE_DB_URL + "/channels/my_key.json");
  }

  @Test
  public void createFirebaseToken() throws Exception {
    Game game = new Game();

    String jwt = firebaseChannel.createFirebaseToken(game, "userId");

    assertThat(jwt).matches("^([\\w+/=-]+\\.){2}[\\w+/=-]+$");
  }

  @Test
  public void firebasePut() throws Exception {
    // Mock out the firebase response. See
    // http://g.co/dv/api-client-library/java/google-http-java-client/unit-testing
    MockHttpTransport mockHttpTransport =
        spy(
            new MockHttpTransport() {
              @Override
              public LowLevelHttpRequest buildRequest(String method, String url)
                  throws IOException {
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
    FirebaseChannel.getInstance().httpTransport = mockHttpTransport;
    Game game = new Game();

    firebaseChannel.firebasePut(FIREBASE_DB_URL + "/my/path", game);

    verify(mockHttpTransport, times(1)).buildRequest("PUT", FIREBASE_DB_URL + "/my/path");
  }

  @Test
  public void firebasePatch() throws Exception {
    // Mock out the firebase response. See
    // http://g.co/dv/api-client-library/java/google-http-java-client/unit-testing
    MockHttpTransport mockHttpTransport =
        spy(
            new MockHttpTransport() {
              @Override
              public LowLevelHttpRequest buildRequest(String method, String url)
                  throws IOException {
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
    FirebaseChannel.getInstance().httpTransport = mockHttpTransport;
    Game game = new Game();

    firebaseChannel.firebasePatch(FIREBASE_DB_URL + "/my/path", game);

    verify(mockHttpTransport, times(1)).buildRequest("PATCH", FIREBASE_DB_URL + "/my/path");
  }

  @Test
  public void firebasePost() throws Exception {
    // Mock out the firebase response. See
    // http://g.co/dv/api-client-library/java/google-http-java-client/unit-testing
    MockHttpTransport mockHttpTransport =
        spy(
            new MockHttpTransport() {
              @Override
              public LowLevelHttpRequest buildRequest(String method, String url)
                  throws IOException {
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
    FirebaseChannel.getInstance().httpTransport = mockHttpTransport;
    Game game = new Game();

    firebaseChannel.firebasePost(FIREBASE_DB_URL + "/my/path", game);

    verify(mockHttpTransport, times(1)).buildRequest("POST", FIREBASE_DB_URL + "/my/path");
  }

  @Test
  public void firebaseGet() throws Exception {
    // Mock out the firebase response. See
    // http://g.co/dv/api-client-library/java/google-http-java-client/unit-testing
    MockHttpTransport mockHttpTransport =
        spy(
            new MockHttpTransport() {
              @Override
              public LowLevelHttpRequest buildRequest(String method, String url)
                  throws IOException {
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
    FirebaseChannel.getInstance().httpTransport = mockHttpTransport;

    firebaseChannel.firebaseGet(FIREBASE_DB_URL + "/my/path");

    verify(mockHttpTransport, times(1)).buildRequest("GET", FIREBASE_DB_URL + "/my/path");
  }

  @Test
  public void firebaseDelete() throws Exception {
    // Mock out the firebase response. See
    // http://g.co/dv/api-client-library/java/google-http-java-client/unit-testing
    MockHttpTransport mockHttpTransport =
        spy(
            new MockHttpTransport() {
              @Override
              public LowLevelHttpRequest buildRequest(String method, String url)
                  throws IOException {
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
    FirebaseChannel.getInstance().httpTransport = mockHttpTransport;

    firebaseChannel.firebaseDelete(FIREBASE_DB_URL + "/my/path");

    verify(mockHttpTransport, times(1)).buildRequest("DELETE", FIREBASE_DB_URL + "/my/path");
  }
}
