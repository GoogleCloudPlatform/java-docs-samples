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

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
public class FilesystemApplicationIT {

  private static final String project = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String suffix = UUID.randomUUID().toString();
  private static final String mntDir =
      System.getenv().getOrDefault("MNT_DIR", "/mnt/nfs/filestore");
  private static final String connector =
      System.getenv().getOrDefault("CONNECTOR", "my-run-connector");
  private static final String ipAddress = System.getenv("IP_ADDRESS");
  private static String service;
  private static String baseUrl;
  private static String idToken;

  @BeforeClass
  public static void setup() throws Exception {
    service = "filesystem" + suffix;

    ProcessBuilder deploy = new ProcessBuilder();
    deploy.command(
        "gcloud",
        "alpha",
        "run",
        "deploy",
        service,
        "--source=.",
        "--region=us-central1",
        "--no-allow-unauthenticated",
        String.format("--vpc-connector=%s", connector),
        "--execution-environment=gen2",
        String.format("--update-env-vars=IP_ADDRESS=%s,FILESHARE_NAME=vol1", ipAddress));

    System.out.println("Start Cloud Build...");
    String output = IOUtils.toString(deploy.start().getInputStream(), StandardCharsets.UTF_8);
    System.out.println("Cloud Build Completed.");

    // Get service URL
    ProcessBuilder getUrl = new ProcessBuilder();
    getUrl.command(
        "gcloud",
        "run",
        "services",
        "describe",
        service,
        "--region=us-central1",
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
