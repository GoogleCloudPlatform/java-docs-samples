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

import com.google.api.gax.rpc.ApiException;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.gson.Gson;
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
    SpringApplication app = new SpringApplication(IdpSqlApplication.class);

    String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
    if (projectId == null) {
      projectId = getProjectId();
    }

    // Initialize Firebase Admin SDK
    GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
    FirebaseOptions options =
        FirebaseOptions.builder().setProjectId(projectId).setCredentials(credentials).build();
    FirebaseApp.initializeApp(options);

    // Set config for Cloud SQL
    String secretId = System.getenv("SECRET_NAME");
    String versionId = System.getenv().getOrDefault("VERSION", "latest");
    HashMap<String, Object> config = getConfig(projectId, secretId, versionId);
    app.setDefaultProperties(config);
    app.run(args);
  }

  /** Register shutdown hook */
  @PreDestroy
  public void tearDown() {
    logger.info(IdpSqlApplication.class.getSimpleName() + ": received SIGTERM.");
    // Spring Boot closes JDBC connections.
    // https://docs.spring.io/spring-framework/docs/3.0.x/spring-framework-reference/html/jdbc.html

    // Flush async logs if needed
    // Current Logback config defaults to immediate flushing of all logs
  }

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

  /** Retrieve config from Secret Manager */
  public static HashMap<String, Object> getConfig(
      String projectId, String secretId, String versionId) throws IOException {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (SecretManagerServiceClient client = SecretManagerServiceClient.create()) {
      // Build the name from the version
      SecretVersionName secretVersionName = SecretVersionName.of(projectId, secretId, versionId);

      // Retrieve secret version
      AccessSecretVersionResponse response = client.accessSecretVersion(secretVersionName);
      String json = response.getPayload().getData().toStringUtf8();

      // Convert JSON secret to a Map
      HashMap<String, Object> config = new Gson().fromJson(json, HashMap.class);
      return config;
    } catch (IOException e) {
      logger.error("Unable to create Secret Manager client: " + e.toString());
      throw new RuntimeException("Unable to retrieve config secrets.");
    } catch (ApiException e) {
      logger.error("Unable to retrieve config secrets: " + e.toString());
      throw new RuntimeException("Unable to retrieve config secrets.");
    }
  }
}
