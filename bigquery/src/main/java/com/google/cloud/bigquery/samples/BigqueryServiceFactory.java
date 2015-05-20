/*
 * Copyright (c) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.cloud.bigquery.samples;

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.bigquery.Bigquery;
import com.google.api.services.bigquery.BigqueryScopes;

import java.io.IOException;
import java.util.Collection;

public class BigqueryServiceFactory {

  private static Bigquery service = null;
  private static Object service_lock = new Object();

  public static Bigquery getService() throws IOException{
    if(service==null){
      synchronized(service_lock){
        if(service==null){
          service=createAuthorizedClient();
        }
      }
    }
    return service;
  }

  // [START get_service]
  private static Bigquery createAuthorizedClient() throws IOException {
    Collection<String> BIGQUERY_SCOPES = BigqueryScopes.all();
    HttpTransport TRANSPORT = new NetHttpTransport();
    JsonFactory JSON_FACTORY = new JacksonFactory();
    GoogleCredential credential = GoogleCredential.getApplicationDefault(TRANSPORT, JSON_FACTORY);
    if(credential.createScopedRequired()){
      credential = credential.createScoped(BIGQUERY_SCOPES);
    }
    return new Bigquery.Builder(TRANSPORT, JSON_FACTORY, credential).setApplicationName("BigQuery Samples").build();
  }
  // [END get_service]

}
