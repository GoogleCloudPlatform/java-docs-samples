package com.example.vision.snippets;

// [START vision_face_detection]

import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.FaceAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DetectFaces {

  public static void detectFaces() throws IOException {
    // TODO(developer): Replace these variables before running the sample.
    String filePath = "path/to/your/image/file.jpg";
    detectFaces(filePath);
  }

  // Detects faces in the specified local image.
  public static void detectFaces(String filePath) throws IOException {
    List<AnnotateImageRequest> requests = new ArrayList<>();

    ByteString imgBytes = ByteString.readFrom(new FileInputStream(filePath));

    Image img = Image.newBuilder().setContent(imgBytes).build();
    Feature feat = Feature.newBuilder().setType(Feature.Type.FACE_DETECTION).build();
    AnnotateImageRequest request =
            AnnotateImageRequest.newBuilder().addFeatures(feat).setImage(img).build();
    requests.add(request);

    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests. After completing all of your requests, call
    // the "close" method on the client to safely clean up any remaining background resources.
    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
      List<AnnotateImageResponse> responses = response.getResponsesList();

      for (AnnotateImageResponse res : responses) {
        if (res.hasError()) {
          System.out.format("Error: %s%n", res.getError().getMessage());
          return;
        }

        // For full list of available annotations, see http://g.co/cloud/vision/docs
        for (FaceAnnotation annotation : res.getFaceAnnotationsList()) {
          System.out.format(
                  "anger: %s%njoy: %s%nsurprise: %s%nposition: %s",
                  annotation.getAngerLikelihood(),
                  annotation.getJoyLikelihood(),
                  annotation.getSurpriseLikelihood(),
                  annotation.getBoundingPoly());
        }
      }
    }
  }
}
// [END vision_face_detection]
