/*
 * Copyright 2016 Google Inc. All Rights Reserved.
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

package com.google.cloud.vision.samples.landmarkdetection;

// [START import_libraries]
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.vision.v1.Vision;
import com.google.api.services.vision.v1.VisionScopes;
import com.google.api.services.vision.v1.model.AnnotateImageRequest;
import com.google.api.services.vision.v1.model.AnnotateImageResponse;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesRequest;
import com.google.api.services.vision.v1.model.BatchAnnotateImagesResponse;
import com.google.api.services.vision.v1.model.EntityAnnotation;
import com.google.api.services.vision.v1.model.Feature;
import com.google.api.services.vision.v1.model.Image;
import com.google.api.services.vision.v1.model.ImageSource;
import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;
// [END import_libraries]

/**
 * A sample application that uses the Vision API to detect landmarks in an image that is hosted on
 * Google Cloud Storage.
 */
@SuppressWarnings("serial")
public class DetectLandmark {
  /**
   * Be sure to specify the name of your application. If the application name is {@code null} or
   * blank, the application will log a warning. Suggested format is "MyCompany-ProductName/1.0".
   */
  private static final String APPLICATION_NAME = "Google-VisionDetectLandmark/1.0";

  private static final int MAX_RESULTS = 4;

  // [START run_application]
  /**
   * Annotates an image using the Vision API.
   */
  public static void main(String[] args) throws IOException, GeneralSecurityException {
    if (args.length != 1) {
      System.err.println("Usage:");
      System.err.printf("\tjava %s gs://<bucket_name>/<object_name>\n",
          DetectLandmark.class.getCanonicalName());
      System.exit(1);
    } else if (!args[0].toLowerCase().startsWith("gs://")) {
      System.err.println("Google Cloud Storage url must start with 'gs://'.");
      System.exit(1);
    }

    DetectLandmark app = new DetectLandmark(getVisionService());
    List<EntityAnnotation> landmarks = app.identifyLandmark(args[0], MAX_RESULTS);
    System.out.printf("Found %d landmark%s\n", landmarks.size(), landmarks.size() == 1 ? "" : "s");
    for (EntityAnnotation annotation : landmarks) {
      System.out.printf("\t%s\n", annotation.getDescription());
    }
  }
  // [END run_application]

  // [START authenticate]
  /**
   * Connects to the Vision API using Application Default Credentials.
   */
  public static Vision getVisionService() throws IOException, GeneralSecurityException {
    GoogleCredential credential =
        GoogleCredential.getApplicationDefault().createScoped(VisionScopes.all());
    JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();
    return new Vision.Builder(GoogleNetHttpTransport.newTrustedTransport(), jsonFactory, credential)
            .setApplicationName(APPLICATION_NAME)
            .build();
  }
  // [END authenticate]

  // [START detect_gcs_object]
  private final Vision vision;

  /**
   * Constructs a {@link DetectLandmark} which connects to the Vision API.
   */
  public DetectLandmark(Vision vision) {
    this.vision = vision;
  }

  /**
   * Gets up to {@code maxResults} landmarks for an image stored at {@code uri}.
   */
  public List<EntityAnnotation> identifyLandmark(String uri, int maxResults) throws IOException {
    AnnotateImageRequest request =
        new AnnotateImageRequest()
            .setImage(new Image().setSource(
                new ImageSource().setGcsImageUri(uri)))
            .setFeatures(ImmutableList.of(
                new Feature()
                    .setType("LANDMARK_DETECTION")
                    .setMaxResults(maxResults)));
    Vision.Images.Annotate annotate =
        vision.images()
            .annotate(new BatchAnnotateImagesRequest().setRequests(ImmutableList.of(request)));

    BatchAnnotateImagesResponse batchResponse = annotate.execute();
    assert batchResponse.getResponses().size() == 1;
    AnnotateImageResponse response = batchResponse.getResponses().get(0);
    if (response.getLandmarkAnnotations() == null) {
      throw new IOException(
          response.getError() != null
              ? response.getError().getMessage()
              : "Unknown error getting image annotations");
    }
    return response.getLandmarkAnnotations();
  }
  // [END detect_gcs_object]
}
