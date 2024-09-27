/*
 * Copyright 2024 Google LLC
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

package com.example.amlai;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.GoogleCredentials;
import java.io.IOException;

// [START antimoneylaunderingai_list_locations]

public class ListLocations {

  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String projectId = "my-project-id";

    listLocations(projectId);
  }

  // Lists all the AML AI API locations for a given Cloud project.
  public static String listLocations(String projectId) throws IOException {
    // Use Application Default Credentials (ADC) to authenticate the requests
    // For more information, see https://cloud.google.com/docs/authentication/production
    GoogleCredentials credential =
        GoogleCredentials.getApplicationDefault()
            .createScoped("https://www.googleapis.com/auth/cloud-platform");

    HttpRequestFactory requestFactory =
        (new NetHttpTransport()).createRequestFactory(new HttpCredentialsAdapter(credential));

    String baseUrl = "https://financialservices.googleapis.com/v1";
    String path = String.format("%s/projects/%s/locations/", baseUrl, projectId);
    GenericUrl url = new GenericUrl(path);
    HttpHeaders httpHeaders = new HttpHeaders().setContentType("application/json; charset=utf-8");

    HttpResponse response = requestFactory.buildGetRequest(url).setHeaders(httpHeaders).execute();
    System.out.println(response.parseAsString());
    return response.parseAsString();
  }
}
// [END antimoneylaunderingai_list_locations]
