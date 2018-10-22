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
import com.google.cloud.videointelligence.v1p2beta1.*;
import com.google.protobuf.ByteString;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

public class TrackObjects {

    // [START video_object_tracking_beta]
    /**
     * Track objects in a video.
     *
     * @param filePath the path to the video file to analyze.
     */
    public static VideoAnnotationResults trackObjects(String filePath) throws Exception {

        try (VideoIntelligenceServiceClient client = VideoIntelligenceServiceClient.create()) {

            // Read file and encode into Base64
            Path path = Paths.get(filePath);
            byte[] data = Files.readAllBytes(path);

            // Create the request
            AnnotateVideoRequest request = AnnotateVideoRequest.newBuilder()
                    .setInputContent(ByteString.copyFrom(data))
                    .addFeatures(Feature.OBJECT_TRACKING)
                    .build();

            // asynchronously perform object tracking on videos
            OperationFuture<AnnotateVideoResponse, AnnotateVideoProgress> response =
                    client.annotateVideoAsync(request);

            System.out.println("Waiting for operation to complete...");
            // The first result is retrieved because a single video was processed.
            VideoAnnotationResults result = response.get(300, TimeUnit.SECONDS)
                    .getAnnotationResults(0);

            // Get only the first annotation for demo purposes.
            ObjectTrackingAnnotation objectTrackingAnnotation = result.getObjectAnnotations(0);

            if (objectTrackingAnnotation.hasEntity()) {
                System.out.println("Entity description: " + objectTrackingAnnotation.getEntity().getDescription());
                System.out.println("Entity id:: " + objectTrackingAnnotation.getEntity().getEntityId());
            }

            if (objectTrackingAnnotation.hasSegment()) {
                VideoSegment videoSegment = objectTrackingAnnotation.getSegment();
                System.out.println(String.format("Segment: %.2fs to %.2fs",
                        videoSegment.getStartTimeOffset().getSeconds() +
                                videoSegment.getStartTimeOffset().getNanos() / 1e9,
                        videoSegment.getEndTimeOffset().getSeconds() +
                                videoSegment.getEndTimeOffset().getNanos() / 1e9));
            }

            System.out.println("Confidence: " + objectTrackingAnnotation.getConfidence());


            // Here we print only the bounding box of the first frame in this segment.
            ObjectTrackingFrame objectTrackingFrame = objectTrackingAnnotation.getFrames(0);
            NormalizedBoundingBox normalizedBoundingBox = objectTrackingFrame.getNormalizedBoundingBox();
            System.out.println(String.format("Time offset of the first frame: %.2fs",
                    objectTrackingFrame.getTimeOffset().getSeconds() +
                            objectTrackingFrame.getTimeOffset().getNanos() / 1e9));

            System.out.println("Bounding box position:");
            System.out.println("\tleft: " + normalizedBoundingBox.getLeft());
            System.out.println("\ttop: " + normalizedBoundingBox.getTop());
            System.out.println("\tright: " + normalizedBoundingBox.getRight());
            System.out.println("\tbottom: " + normalizedBoundingBox.getBottom());
            return result;
        }
    }
    // [END video_object_tracking_beta]

    // [START video_object_tracking_gcs_beta]
    /**
     * Track objects in a video.
     *
     * @param gcsUri the path to the video file to analyze.
     */
    public static VideoAnnotationResults trackObjectsGcs(String gcsUri) throws Exception {

        try (VideoIntelligenceServiceClient client = VideoIntelligenceServiceClient.create()) {
            // Create the request
            AnnotateVideoRequest request = AnnotateVideoRequest.newBuilder()
                    .setInputUri(gcsUri)
                    .addFeatures(Feature.OBJECT_TRACKING)
                    .build();

            // asynchronously perform object tracking on videos
            OperationFuture<AnnotateVideoResponse, AnnotateVideoProgress> response =
                    client.annotateVideoAsync(request);

            System.out.println("Waiting for operation to complete...");
            // The first result is retrieved because a single video was processed.
            VideoAnnotationResults result = response.get(300, TimeUnit.SECONDS)
                    .getAnnotationResults(0);

            // Get only the first annotation for demo purposes.
            ObjectTrackingAnnotation objectTrackingAnnotation = result.getObjectAnnotations(0);

            if (objectTrackingAnnotation.hasEntity()) {
                System.out.println("Entity description: " + objectTrackingAnnotation.getEntity().getDescription());
                System.out.println("Entity id:: " + objectTrackingAnnotation.getEntity().getEntityId());
            }

            if (objectTrackingAnnotation.hasSegment()) {
                VideoSegment videoSegment = objectTrackingAnnotation.getSegment();
                System.out.println(String.format("Segment: %.2fs to %.2fs",
                        videoSegment.getStartTimeOffset().getSeconds() +
                                videoSegment.getStartTimeOffset().getNanos() / 1e9,
                        videoSegment.getEndTimeOffset().getSeconds() +
                                videoSegment.getEndTimeOffset().getNanos() / 1e9));
            }

            System.out.println("Confidence: " + objectTrackingAnnotation.getConfidence());


            // Here we print only the bounding box of the first frame in this segment.
            ObjectTrackingFrame objectTrackingFrame = objectTrackingAnnotation.getFrames(0);
            NormalizedBoundingBox normalizedBoundingBox = objectTrackingFrame.getNormalizedBoundingBox();
            System.out.println(String.format("Time offset of the first frame: %.2fs",
                    objectTrackingFrame.getTimeOffset().getSeconds() +
                            objectTrackingFrame.getTimeOffset().getNanos() / 1e9));

            System.out.println("Bounding box position:");
            System.out.println("\tleft: " + normalizedBoundingBox.getLeft());
            System.out.println("\ttop: " + normalizedBoundingBox.getTop());
            System.out.println("\tright: " + normalizedBoundingBox.getRight());
            System.out.println("\tbottom: " + normalizedBoundingBox.getBottom());
            return result;
        }
    }
    // [END video_object_tracking_gcs_beta]
}

