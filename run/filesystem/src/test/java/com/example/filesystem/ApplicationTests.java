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

package com.example.filesystem;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.InterruptedByTimeoutException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {

  private static final String project = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String suffix = UUID.randomUUID().toString();
  private static final String mntDir = "/mnt/nfs/filestore";
  private static final String connector =
      System.getenv().getOrDefault("CONNECTOR", "my-run-connector");
  private static final String ipAddress = System.getenv("FILESTORE_IP_ADDRESS");
  private static String service;
  private static String baseUrl;
  private static String idToken;

  @BeforeClass
  public static void setup() throws Exception {
    if (ipAddress == null || ipAddress.equals("")) {
      throw new RuntimeException("\"FILESTORE_IP_ADDRESS\" not found in environment.");
    }
    if (project == null || project.equals("")) {
      throw new RuntimeException("\"GOOGLE_CLOUD_PROJECT\" not found in environment.");
    }
    service = "filesystem" + suffix;

    // Deploy the Cloud Run service
    ProcessBuilder deploy = new ProcessBuilder();
    deploy.command(
        "gcloud",
        "run",
        "deploy",
        service,
        "--source",
        ".",
        "--region=us-central1",
        "--no-allow-unauthenticated",
        "--project=" + project,
        String.format("--vpc-connector=%s", connector),
        "--execution-environment=gen2",
        String.format("--update-env-vars=FILESTORE_IP_ADDRESS=%s,FILE_SHARE_NAME=vol1", ipAddress));

    deploy.redirectErrorStream(true);
    System.out.println("Start Cloud Run deployment of service: " + service);
    Process p = deploy.start();
    // Set timeout
    if (!p.waitFor(10, TimeUnit.MINUTES)) {
      p.destroy();
      System.out.println("Process timed out.");
      throw new InterruptedByTimeoutException();
    }
    // Read process output
    BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
    String line;
    while ((line = in.readLine()) != null) {
      System.out.println(line);
    }
    in.close();
    System.out.println(String.format("Cloud Run service, %s, deployed.", service));

    // Get service URL
    ProcessBuilder getUrl = new ProcessBuilder();
    getUrl.command(
        "gcloud",
        "run",
        "services",
        "describe",
        service,
        "--region=us-central1",
        "--project=" + project,
        "--format=value(status.url)");
    baseUrl = IOUtils.toString(getUrl.start().getInputStream(), StandardCharsets.UTF_8).trim();
    if (baseUrl == null || baseUrl.equals("")) {
      throw new RuntimeException("Base URL not found.");
    }

    // Get Token
    ProcessBuilder getToken = new ProcessBuilder();
    getToken.command("gcloud", "auth", "print-identity-token");
    idToken = IOUtils.toString(getToken.start().getInputStream(), StandardCharsets.UTF_8).trim();
  }

  @AfterClass
  public static void cleanup() throws IOException, InterruptedException {
    ProcessBuilder delete = new ProcessBuilder();
    delete.command(
        "gcloud",
        "run",
        "services",
        "delete",
        service,
        "--quiet",
        "--region=us-central1",
        "--project=" + project);

    System.out.println("Deleting Cloud Run service: " + service);
    Process p = delete.start();
    p.waitFor(5, TimeUnit.MINUTES);
  }

  public Response authenticatedRequest(String url) throws IOException {
    OkHttpClient ok =
        new OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build();

    // Instantiate HTTP request
    Request request =
        new Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer " + idToken)
            .get()
            .build();

    Response response = ok.newCall(request).execute();
    return response;
  }

  @Test
  public void returns_ok() throws IOException {
    Response indexResponse = authenticatedRequest(baseUrl);
    assertEquals(indexResponse.code(), 403); // Redirect causes 403

    String mntPath = baseUrl + mntDir;
    Response mntResponse = authenticatedRequest(mntPath);
    assertEquals(mntResponse.code(), 200);
    assertTrue(mntResponse.body().string().contains("test-"));
  }
}
