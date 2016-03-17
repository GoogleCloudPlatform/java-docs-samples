/**
 * Copyright (c) 2015 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
// [START all]
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.cloudmonitoring.CloudMonitoring;
import com.google.api.services.cloudmonitoring.CloudMonitoringScopes;

import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Simple command-line program to demonstrate connecting to and retrieving data
 * from the Google Cloud Monitoring API using application default credentials.
 * Please see README.md on instructions to run.
 */
public final class CloudMonitoringAuthSample {

  /**
   * The metric that we want to fetch.
   */
  private static final String METRIC =
      "compute.googleapis.com/instance/disk/read_ops_count";

  /**
   * The end of the time interval to fetch.
   */
  private static final String YOUNGEST = "2015-01-01T00:00:00Z";

  /**
   * Utility class doesn't need to be instantiated.
   */
  private CloudMonitoringAuthSample() { }


  /**
   * Builds and returns a CloudMonitoring service object authorized with the
   * application default credentials.
   *
   * @return CloudMonitoring service object that is ready to make requests.
   * @throws GeneralSecurityException if authentication fails.
   * @throws IOException if authentication fails.
   */
  private static CloudMonitoring authenticate()
      throws GeneralSecurityException, IOException {
    // Grab the Application Default Credentials from the environment.
    GoogleCredential credential = GoogleCredential.getApplicationDefault()
        .createScoped(CloudMonitoringScopes.all());

    // Create and return the CloudMonitoring service object
    HttpTransport httpTransport = new NetHttpTransport();
    JsonFactory jsonFactory = new JacksonFactory();
    CloudMonitoring service = new CloudMonitoring.Builder(httpTransport,
        jsonFactory, credential)
        .setApplicationName("Demo")
        .build();
    return service;
  }

  /**
   * Query the Google Cloud Monitoring API using a service account and print the
   * result to the console.
   *
   * @param args The first arg should be the project name you'd like to inspect.
   * @throws Exception if something goes wrong.
   */
  public static void main(final String[] args) throws Exception {
    if (args.length != 1) {
      System.err.println(String.format("Usage: %s <project-name>",
            CloudMonitoringAuthSample.class.getSimpleName()));
      return;
    }

    String project = args[0];

    // Create an authorized API client
    CloudMonitoring cloudmonitoring = authenticate();

    CloudMonitoring.Timeseries.List timeseriesListRequest =
        cloudmonitoring.timeseries().list(project, METRIC, YOUNGEST);

    System.out.println("Timeseries.list raw response:");
    System.out.println(timeseriesListRequest.execute().toPrettyString());


  }
}
// [END all]
