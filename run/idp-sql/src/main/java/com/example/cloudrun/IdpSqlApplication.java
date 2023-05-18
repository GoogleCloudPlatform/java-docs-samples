/*
 * Copyright 2021 Google LLC
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

package com.example.cloudrun;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import javax.annotation.PreDestroy;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IdpSqlApplication {
  private static final Logger logger = LoggerFactory.getLogger(IdpSqlApplication.class);

  public static void main(String[] args) throws IOException {
    String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
    if (projectId == null) {
      projectId = getProjectId();
    }

    // Initialize Firebase Admin SDK
    GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
    FirebaseOptions options =
        FirebaseOptions.builder().setProjectId(projectId).setCredentials(credentials).build();
    FirebaseApp.initializeApp(options);

    // Retrieve config for Cloud SQL
    HashMap<String, Object> config = getConfig();

    // Set the Cloud SQL config and start app
    SpringApplication app = new SpringApplication(IdpSqlApplication.class);
    app.setDefaultProperties(config);
    app.run(args);
  }

  // [START cloudrun_sigterm_handler]
  /** Register shutdown hook */
  @PreDestroy
  public void tearDown() {
    logger.info(IdpSqlApplication.class.getSimpleName() + ": received SIGTERM.");
    // Spring Boot closes JDBC connections.
    // https://docs.spring.io/spring-framework/docs/3.0.x/spring-framework-reference/html/jdbc.html

    // Flush async logs if needed
    // Current Logback config defaults to immediate flushing of all logs
  }
  // [END cloudrun_sigterm_handler]

  /** Retrieve project Id from metadata server Set $GOOGLE_CLOUD_PROJECT env var to run locally */
  public static String getProjectId() {
    OkHttpClient ok =
        new OkHttpClient.Builder()
            .readTimeout(500, TimeUnit.MILLISECONDS)
            .writeTimeout(500, TimeUnit.MILLISECONDS)
            .build();

    String metadataUrl = "http://metadata.google.internal/computeMetadata/v1/project/project-id";
    Request request =
        new Request.Builder().url(metadataUrl).addHeader("Metadata-Flavor", "Google").get().build();

    try {
      Response response = ok.newCall(request).execute();
      return response.body().string();
    } catch (IOException e) {
      logger.error("Error retrieving the project Id.");
      throw new RuntimeException("Unable to retrieve project Id.");
    }
  }

  @SuppressWarnings("unchecked")
  // [START cloudrun_user_auth_secrets]
  /** Retrieve config from Secret Manager */
  public static HashMap<String, Object> getConfig() {
    String secret = System.getenv("CLOUD_SQL_CREDENTIALS_SECRET");
    if (secret == null) {
      throw new IllegalStateException("\"CLOUD_SQL_CREDENTIALS_SECRET\" is required.");
    }
    try {
      HashMap<String, Object> config = new Gson().fromJson(secret, HashMap.class);
      return config;
    } catch (JsonSyntaxException e) {
      logger.error(
          "Unable to parse secret from Secret Manager. Make sure that it is JSON formatted: "
              + e);
      throw new RuntimeException(
          "Unable to parse secret from Secret Manager. Make sure that it is JSON formatted.");
    }
  }
  // [END cloudrun_user_auth_secrets]
}
