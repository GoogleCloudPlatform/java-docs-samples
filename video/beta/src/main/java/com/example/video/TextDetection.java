/*
 * Copyright 2018 Google LLC
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

package com.example.video;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.videointelligence.v1p2beta1.AnnotateVideoProgress;
import com.google.cloud.videointelligence.v1p2beta1.AnnotateVideoRequest;
import com.google.cloud.videointelligence.v1p2beta1.AnnotateVideoResponse;
import com.google.cloud.videointelligence.v1p2beta1.Feature;
import com.google.cloud.videointelligence.v1p2beta1.NormalizedVertex;
import com.google.cloud.videointelligence.v1p2beta1.TextAnnotation;
import com.google.cloud.videointelligence.v1p2beta1.TextFrame;
import com.google.cloud.videointelligence.v1p2beta1.TextSegment;
import com.google.cloud.videointelligence.v1p2beta1.VideoAnnotationResults;
import com.google.cloud.videointelligence.v1p2beta1.VideoIntelligenceServiceClient;
import com.google.protobuf.ByteString;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class TextDetection {

  // [START video_detect_text_beta]
  /**
   * Detect text in a video.
   *
   * @param filePath the path to the video file to analyze.
   */
  public static VideoAnnotationResults detectText(String filePath) throws Exception {

    try (VideoIntelligenceServiceClient client = VideoIntelligenceServiceClient.create()) {

      // Read file
      Path path = Paths.get(filePath);
      byte[] data = Files.readAllBytes(path);

      // Create the request
      AnnotateVideoRequest request = AnnotateVideoRequest.newBuilder()
          .setInputContent(ByteString.copyFrom(data))
          .addFeatures(Feature.TEXT_DETECTION)
          .build();

      // asynchronously perform object tracking on videos
      OperationFuture<AnnotateVideoResponse, AnnotateVideoProgress> response =
          client.annotateVideoAsync(request);

      System.out.println("Waiting for operation to complete...");
      // The first result is retrieved because a single video was processed.
      VideoAnnotationResults result = response.get(300, TimeUnit.SECONDS)
          .getAnnotationResults(0);

      // Get only the first annotation for demo purposes.
      TextAnnotation textAnnotation = result.getTextAnnotations(0);

      System.out.println("Text: " + textAnnotation.getText());

      // Get the first text segment.
      TextSegment textSegment = textAnnotation.getSegments(0);

      System.out.println(String.format("Start time: %.2f",
          textSegment.getSegment().getStartTimeOffset().getSeconds()
              + textSegment.getSegment().getStartTimeOffset().getNanos() / 1e9));

      System.out.println(String.format("End time: %.2f",
          textSegment.getSegment().getEndTimeOffset().getSeconds()
              + textSegment.getSegment().getEndTimeOffset().getNanos() / 1e9));

      System.out.println("Confidence: " + textSegment.getConfidence());

      // Show the first result for the first frame in the segment.
      TextFrame textFrame = textSegment.getFrames(0);

      System.out.println("Time offset for the first frame: "
          + (textFrame.getTimeOffset().getSeconds()
          + textFrame.getTimeOffset().getNanos() / 1e9));

      System.out.println("Rotated Bounding Box Vertices:");
      for (NormalizedVertex normalizedVertex :
          textFrame.getRotatedBoundingBox().getVerticesList()) {
        System.out.println(String.format("\tVertex.x: %.2f, Vertex.y: %.2f",
            normalizedVertex.getX(),
            normalizedVertex.getY()));
      }

      return result;
    }
  }
  // [END video_detect_text_beta]

  // [START video_detect_text_gcs_beta]
  /**
   * Detect Text in a video.
   *
   * @param gcsUri the path to the video file to analyze.
   */
  public static VideoAnnotationResults detectTextGcs(String gcsUri) throws Exception {

    try (VideoIntelligenceServiceClient client = VideoIntelligenceServiceClient.create()) {

      // Create the request
      AnnotateVideoRequest request = AnnotateVideoRequest.newBuilder()
          .setInputUri(gcsUri)
          .addFeatures(Feature.TEXT_DETECTION)
          .build();

      // asynchronously perform object tracking on videos
      OperationFuture<AnnotateVideoResponse, AnnotateVideoProgress> response =
          client.annotateVideoAsync(request);

      System.out.println("Waiting for operation to complete...");
      // The first result is retrieved because a single video was processed.
      VideoAnnotationResults result = response.get(300, TimeUnit.SECONDS)
          .getAnnotationResults(0);

      // Get only the first annotation for demo purposes.
      TextAnnotation textAnnotation = result.getTextAnnotations(0);

      System.out.println("Text: " + textAnnotation.getText());

      // Get the first text segment.
      TextSegment textSegment = textAnnotation.getSegments(0);

      System.out.println(String.format("Start time: %.2f",
          textSegment.getSegment().getStartTimeOffset().getSeconds()
              + textSegment.getSegment().getStartTimeOffset().getNanos() / 1e9));

      System.out.println(String.format("End time: %.2f",
          textSegment.getSegment().getEndTimeOffset().getSeconds()
              + textSegment.getSegment().getEndTimeOffset().getNanos() / 1e9));

      System.out.println("Confidence: " + textSegment.getConfidence());

      // Show the first result for the first frame in the segment.
      TextFrame textFrame = textSegment.getFrames(0);

      System.out.println("Time offset for the first frame: "
          + (textFrame.getTimeOffset().getSeconds()
          + textFrame.getTimeOffset().getNanos() / 1e9));

      System.out.println("Rotated Bounding Box Vertices:");
      for (NormalizedVertex normalizedVertex :
          textFrame.getRotatedBoundingBox().getVerticesList()) {
        System.out.println(String.format("\tVertex.x: %.2f, Vertex.y: %.2f",
            normalizedVertex.getX(),
            normalizedVertex.getY()));
      }

      return result;
    }
  }
  // [END video_detect_text_gcs_beta]
}
