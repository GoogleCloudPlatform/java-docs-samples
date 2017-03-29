/*
 * Copyright (c) 2016 Google Inc.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */

package com.example.cloud.tasks;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.cloudtasks.v2beta2.CloudTasks;
import com.google.api.services.cloudtasks.v2beta2.CloudTasksScopes;

import java.io.IOException;
import java.security.GeneralSecurityException;

/** Code shared between Pull Queue and App Engine Queue examples. */
public class Common {

  /**
   * Builds and returns a CloudTask service object authorized with the application default
   * credentials.
   *
   * @return CloudTasks service client object that is ready to make requests.
   * @throws GeneralSecurityException if authentication fails.
   * @throws IOException if authentication fails.
   */
  public static CloudTasks authenticate() throws GeneralSecurityException, IOException {
    // Grab the Application Default Credentials from the environment.
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(CloudTasksScopes.all());

    // Create and return the CloudTasks service object
    HttpTransport httpTransport = new NetHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();
    CloudTasks service =
        new CloudTasks.Builder(httpTransport, jsonFactory, credential)
            .setApplicationName("Cloud Tasks Sample")
            .build();
    return service;
  }
}
