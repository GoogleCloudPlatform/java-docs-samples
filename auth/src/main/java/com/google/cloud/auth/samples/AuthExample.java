/*
 * Copyright 2018 Google Inc.
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

package com.google.cloud.auth.samples;

import com.google.api.gax.paging.Page;
import com.google.auth.appengine.AppEngineCredentials;
import com.google.auth.oauth2.ComputeEngineCredentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.common.collect.Lists;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Demonstrate various ways to authenticate requests using Cloud Storage as an example call.
 */
public class AuthExample {
  // [START auth_cloud_implicit]
  static void authImplicit() {
    // If you don't specify credentials when constructing the client, the client library will
    // look for credentials via the environment variable GOOGLE_APPLICATION_CREDENTIALS.
    Storage storage = StorageOptions.getDefaultInstance().getService();

    System.out.println("Buckets:");
    Page<Bucket> buckets = storage.list();
    for (Bucket bucket : buckets.iterateAll()) {
      System.out.println(bucket.toString());
    }
  }
  // [END auth_cloud_implicit]

  // [START auth_cloud_explicit]
  static void authExplicit(String jsonPath) throws IOException {
    // You can specify a credential file by providing a path to GoogleCredentials.
    // Otherwise credentials are read from the GOOGLE_APPLICATION_CREDENTIALS environment variable.
    GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(jsonPath))
          .createScoped(Lists.newArrayList("https://www.googleapis.com/auth/cloud-platform"));
    Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

    System.out.println("Buckets:");
    Page<Bucket> buckets = storage.list();
    for (Bucket bucket : buckets.iterateAll()) {
      System.out.println(bucket.toString());
    }
  }
  // [END auth_cloud_explicit]

  // [START auth_cloud_explicit_compute_engine]
  static void authCompute() {
    // Explicitly request service account credentials from the compute engine instance.
    GoogleCredentials credentials = ComputeEngineCredentials.create();
    Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

    System.out.println("Buckets:");
    Page<Bucket> buckets = storage.list();
    for (Bucket bucket : buckets.iterateAll()) {
      System.out.println(bucket.toString());
    }
  }
  // [END auth_cloud_explicit_compute_engine]

  // [START auth_cloud_explicit_app_engine]
  static void authAppEngineStandard() throws IOException {
    // Explicitly request service account credentials from the app engine standard instance.
    GoogleCredentials credentials = AppEngineCredentials.getApplicationDefault();
    Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

    System.out.println("Buckets:");
    Page<Bucket> buckets = storage.list();
    for (Bucket bucket : buckets.iterateAll()) {
      System.out.println(bucket.toString());
    }
  }
  // [END auth_cloud_explicit_app_engine]

  public static void main(String[] args) throws IOException {
    if (args.length == 0) {
      authImplicit();
      return;
    }
    if ("explicit".equals(args[0])) {
      if (args.length >= 2) {
        authExplicit(args[1]);
      } else {
        throw new IllegalArgumentException("Path to credential file required with 'explicit'.");
      }
      return;
    }
    if ("compute".equals(args[0])) {
      authCompute();
      return;
    }
    if ("appengine".equals(args[0])) {
      authAppEngineStandard();
      return;
    }
    authImplicit();
  }
}
