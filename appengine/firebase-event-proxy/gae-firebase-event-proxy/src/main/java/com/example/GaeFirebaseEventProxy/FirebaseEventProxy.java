package com.example.GaeFirebaseEventProxy;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.firebase.client.AuthData;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.firebase.security.token.TokenGenerator;
import com.google.appengine.api.utils.SystemProperty;

public class FirebaseEventProxy {

  private static final Logger log = Logger.getLogger(FirebaseEventProxy.class.getName());

  private String firebaseAuthToken;

  public FirebaseEventProxy() {
    this.firebaseAuthToken = this.getFirebaseAuthToken(this.getFirebaseSecret());
  }

  public void start() {
    String FIREBASE_LOCATION = "https://gae-fb-proxy.firebaseio.com/";
    Firebase firebase = new Firebase(FIREBASE_LOCATION);
    // Authenticate with Firebase
    firebase.authWithCustomToken(this.firebaseAuthToken, new Firebase.AuthResultHandler() {
      @Override
      public void onAuthenticationError(FirebaseError error) {
        log.severe("Firebase login error: " + error.getMessage());
      }

      @Override
      public void onAuthenticated(AuthData auth) {
        log.info("Firebase login successful");
      }
    });

    // Subscribe to value events. Depending on use case, you may want to subscribe to child events
    // through childEventListener.
    firebase.addValueEventListener(new ValueEventListener() {
      @Override
      public void onDataChange(DataSnapshot snapshot) {
        if (snapshot.exists()) {
          try {
            // Convert value to JSON using Jackson
            String json = new ObjectMapper().writeValueAsString(snapshot.getValue());
            // Replace the URL with the url of your own listener app.
            URL dest = new URL("http://gae-firebase-listener-python.appspot.com/log");
            HttpURLConnection connection = (HttpURLConnection) dest.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            // Rely on X-Appengine-Inbound-Appid to authenticate. Turning off redirects is
            // required to enable.
            connection.setInstanceFollowRedirects(false);
            // Fill out header if in dev environment
            if (SystemProperty.environment.value() != SystemProperty.Environment.Value.Production) {
              connection.setRequestProperty("X-Appengine-Inbound-Appid", "dev-instance");
            }
            // Put Firebase data into http request
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("&fbSnapshot=");
            stringBuilder.append(URLEncoder.encode(json, "UTF-8"));
            connection.getOutputStream().write(stringBuilder.toString().getBytes());
            if (connection.getResponseCode() != 200) {
              log.severe("Forwarding failed");
            } else {
              log.info("Sent: " + json);
            }
          } catch (JsonProcessingException e) {
            log.severe("Unable to convert Firebase response to JSON: " + e.getMessage());
          } catch (IOException e) {
            log.severe("Error in connecting to app engine: " + e.getMessage());
          }
        }
      }

      @Override
      public void onCancelled(FirebaseError error) {
        log.severe("Firebase connection cancelled: " + error.getMessage());
      }
    });
  }

  private String getFirebaseSecret() {
    Properties props = new Properties();
    try {
      // Read from src/main/webapp/firebase-secrets.properties
      InputStream inputStream = new FileInputStream("firebase-secret.properties");
      props.load(inputStream);
      return props.getProperty("firebaseSecret");
    } catch (java.net.MalformedURLException e) {
      throw new RuntimeException(
          "Error reading firebase secrets from file: src/main/webapp/firebase-sercrets.properties: "
              + e.getMessage());
    } catch (IOException e) {
      throw new RuntimeException(
          "Error reading firebase secrets from file: src/main/webapp/firebase-sercrets.properties: "
              + e.getMessage());
    }
  }

  private String getFirebaseAuthToken(String firebaseSecret) {
    Map<String, Object> authPayload = new HashMap<String, Object>();
    // uid and provider will have to match what you have in your firebase security rules
    authPayload.put("uid", "gae-firebase-event-proxy");
    authPayload.put("provider", "com.example");
    TokenGenerator tokenGenerator = new TokenGenerator(firebaseSecret);
    return tokenGenerator.createToken(authPayload);
  }

}
