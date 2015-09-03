/*
 * Copyright (c) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not  use this file except in compliance with the License. You may obtain a
 * copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.bigquery.samples;

// [START imports]
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.BigqueryScopes;
// [END imports]

import java.io.IOException;
import java.util.Collection;

/**
 * This class creates our Service to connect to Bigquery including auth.
 */
public final class BigqueryServiceFactory {

  /**
   * Private constructor to disable creation of this utility Factory class.
   */
  private BigqueryServiceFactory() {

  }

  /**
   * Singleton service used through the app.
   */
  private static Bigquery service = null;

  /**
   * Mutex created to create the singleton in thread-safe fashion.
   */
  private static Object serviceLock = new Object();

  /**
   * Threadsafe Factory that provides an authorized Bigquery service.
   * @return The Bigquery service
   * @throws IOException Thronw if there is an error connecting to Bigquery.
   */
  public static Bigquery getService() throws IOException {
    if (service == null) {
      synchronized (serviceLock) {
        if (service == null) {
          service = createAuthorizedClient();
        }
      }
    }
    return service;
  }

  /**
   * Creates an authorized client to Google Bigquery.
   *
   * @return The BigQuery Service
   * @throws IOException Thrown if there is an error connecting
   */
  // [START get_service]
  private static Bigquery createAuthorizedClient() throws IOException {
    // Create the credential
    HttpTransport transport = new NetHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();
    GoogleCredential credential =  GoogleCredential.getApplicationDefault(transport, jsonFactory);

    // Depending on the environment that provides the default credentials (e.g. Compute Engine, App
    // Engine), the credentials may require us to specify the scopes we need explicitly.
    // Check for this case, and inject the Bigquery scope if required.
    if (credential.createScopedRequired()) {
      Collection<String> bigqueryScopes = BigqueryScopes.all();
      credential = credential.createScoped(bigqueryScopes);
    }

    return new Bigquery.Builder(transport, jsonFactory, credential)
        .setApplicationName("BigQuery Samples").build();
  }
  // [END get_service]

}
