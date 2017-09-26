/*
 * Copyright 2016 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.appengine.firetactoe;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.appengine.http.UrlFetchTransport;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.util.Preconditions;
import com.google.appengine.api.appidentity.AppIdentityService;
import com.google.appengine.api.appidentity.AppIdentityServiceFactory;
import com.google.common.io.BaseEncoding;
import com.google.common.io.CharStreams;
import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;

/**
 * Utility functions for communicating with the realtime communication channel using Firebase.
 * In this app, we use Firebase as a communication bus to push the state of the board to all clients
 * - that is, players of the game. This class contains the methods used to communicate with
 * Firebase.
 */
public class FirebaseChannel {
  private static final String FIREBASE_SNIPPET_PATH = "/WEB-INF/view/firebase_config.jspf";
  static InputStream firebaseConfigStream = null;
  private static final Collection FIREBASE_SCOPES = Arrays.asList(
      "https://www.googleapis.com/auth/firebase.database",
      "https://www.googleapis.com/auth/userinfo.email"
  );
  private static final String IDENTITY_ENDPOINT =
      "https://identitytoolkit.googleapis.com/google.identity.identitytoolkit.v1.IdentityToolkit";

  private String firebaseDbUrl;
  private GoogleCredential credential;
  // Keep this a package-private member variable, so that it can be mocked for unit tests
  HttpTransport httpTransport;

  private static FirebaseChannel instance;

  /**
   * FirebaseChannel is a singleton, since it's just utility functions.
   * The class derives auth information when first instantiated.
   */
  public static FirebaseChannel getInstance(ServletContext servletContext) {
    if (instance == null) {
      instance = new FirebaseChannel(servletContext);
    }
    return instance;
  }

  /**
   * Construct the singleton, with derived auth information. The Firebase database url is derived
   * from the snippet that we provide to the client code, to guarantee that the client and the
   * server are communicating with the same Firebase database. The auth credentials we'll use to
   * communicate with Firebase is derived from App Engine's default credentials, and given
   * Firebase's OAuth scopes.
   */
  private FirebaseChannel(ServletContext servletContext) {
    try {
      // This variables exist primarily so it can be stubbed out in unit tests.
      if (null == firebaseConfigStream) {
        Preconditions.checkNotNull(servletContext,
            "Servlet context expected to initialize Firebase channel");
        firebaseConfigStream = servletContext.getResourceAsStream(FIREBASE_SNIPPET_PATH);
      }

      String firebaseSnippet = CharStreams.toString(new InputStreamReader(
          firebaseConfigStream, StandardCharsets.UTF_8));
      firebaseDbUrl = parseFirebaseUrl(firebaseSnippet);

      credential = GoogleCredential.getApplicationDefault().createScoped(FIREBASE_SCOPES);
      httpTransport = UrlFetchTransport.getDefaultInstance();
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Parses out the Firebase database url from the client-side code snippet.
   * The code snippet is a piece of javascript that defines an object with the key 'databaseURL'. So
   * look for that key, then parse out its quote-surrounded value.
   */
  private static String parseFirebaseUrl(String firebaseSnippet) {
    int idx = firebaseSnippet.indexOf("databaseURL");
    if (-1 == idx) {
      throw new RuntimeException(
          "Please copy your Firebase web snippet into " + FIREBASE_SNIPPET_PATH);
    }
    idx = firebaseSnippet.indexOf(':', idx);
    int openQuote = firebaseSnippet.indexOf('"', idx);
    int closeQuote = firebaseSnippet.indexOf('"', openQuote + 1);
    return firebaseSnippet.substring(openQuote + 1, closeQuote);
  }

  public void sendFirebaseMessage(String channelKey, Game game)
      throws IOException {
    // Make requests auth'ed using Application Default Credentials
    HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential);
    GenericUrl url = new GenericUrl(
        String.format("%s/channels/%s.json", firebaseDbUrl, channelKey));
    HttpResponse response = null;

    try {
      if (null == game) {
        response = requestFactory.buildDeleteRequest(url).execute();
      } else {
        String gameJson = new Gson().toJson(game);
        response = requestFactory.buildPatchRequest(
            url, new ByteArrayContent("application/json", gameJson.getBytes())).execute();
      }

      if (response.getStatusCode() != 200) {
        throw new RuntimeException(
            "Error code while updating Firebase: " + response.getStatusCode());
      }

    } finally {
      if (null != response) {
        response.disconnect();
      }
    }
  }

  /**
   * Create a secure JWT token for the given userId.
   */
  public String createFirebaseToken(Game game, String userId) {
    final AppIdentityService appIdentity = AppIdentityServiceFactory.getAppIdentityService();
    final BaseEncoding base64 = BaseEncoding.base64();

    String header = base64.encode("{\"typ\":\"JWT\",\"alg\":\"RS256\"}".getBytes());

    // Construct the claim
    String channelKey = game.getChannelKey(userId);
    String clientEmail = appIdentity.getServiceAccountName();
    long epochTime = System.currentTimeMillis() / 1000;
    long expire = epochTime + 60 * 60; // an hour from now

    Map<String, Object> claims = new HashMap<String, Object>();
    claims.put("iss", clientEmail);
    claims.put("sub", clientEmail);
    claims.put("aud", IDENTITY_ENDPOINT);
    claims.put("uid", channelKey);
    claims.put("iat", epochTime);
    claims.put("exp", expire);

    String payload = base64.encode(new Gson().toJson(claims).getBytes());
    String toSign = String.format("%s.%s", header, payload);
    AppIdentityService.SigningResult result = appIdentity.signForApp(toSign.getBytes());
    return String.format("%s.%s", toSign, base64.encode(result.getSignature()));
  }

  // The following methods are to illustrate making various calls to Firebase from App Engine
  // Standard
  public HttpResponse firebasePut(String path, Object object) throws IOException {
    // Make requests auth'ed using Application Default Credentials
    Credential credential = GoogleCredential.getApplicationDefault().createScoped(FIREBASE_SCOPES);
    HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential);

    String json = new Gson().toJson(object);
    GenericUrl url = new GenericUrl(path);

    return requestFactory.buildPutRequest(
        url, new ByteArrayContent("application/json", json.getBytes())).execute();
  }

  public HttpResponse firebasePatch(String path, Object object) throws IOException {
    // Make requests auth'ed using Application Default Credentials
    Credential credential = GoogleCredential.getApplicationDefault().createScoped(FIREBASE_SCOPES);
    HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential);

    String json = new Gson().toJson(object);
    GenericUrl url = new GenericUrl(path);

    return requestFactory.buildPatchRequest(
        url, new ByteArrayContent("application/json", json.getBytes())).execute();
  }

  public HttpResponse firebasePost(String path, Object object) throws IOException {
    // Make requests auth'ed using Application Default Credentials
    Credential credential = GoogleCredential.getApplicationDefault().createScoped(FIREBASE_SCOPES);
    HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential);

    String json = new Gson().toJson(object);
    GenericUrl url = new GenericUrl(path);

    return requestFactory.buildPostRequest(
        url, new ByteArrayContent("application/json", json.getBytes())).execute();
  }

  public HttpResponse firebaseGet(String path) throws IOException {
    // Make requests auth'ed using Application Default Credentials
    Credential credential = GoogleCredential.getApplicationDefault().createScoped(FIREBASE_SCOPES);
    HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential);

    GenericUrl url = new GenericUrl(path);

    return requestFactory.buildGetRequest(url).execute();
  }

  public HttpResponse firebaseDelete(String path) throws IOException {
    // Make requests auth'ed using Application Default Credentials
    Credential credential = GoogleCredential.getApplicationDefault().createScoped(FIREBASE_SCOPES);
    HttpRequestFactory requestFactory = httpTransport.createRequestFactory(credential);

    GenericUrl url = new GenericUrl(path);

    return requestFactory.buildDeleteRequest(url).execute();
  }
}
