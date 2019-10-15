/*
 * Copyright 2019 Google LLC
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

package com.example.cloudrun;

import com.google.gson.JsonObject;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Feature.Type;
import com.google.cloud.vision.v1.GcsSource;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageSource;
import com.google.cloud.vision.v1.SafeSearchAnnotation;

import java.io.OutputStream;
import java.io.ByteArrayOutputStream;
// import magick.MagickImage;
// import magick.ImageInfo;
// import magick.MagickException;
import org.apache.commons.io.IOUtils;
import java.nio.file.Path;
import java.nio.file.FileSystems;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;


public class ImageMagick {
  private static final String BLURRED_BUCKET_NAME = System.getenv("BLURRED_BUCKET_NAME");
  private static Storage storage = StorageOptions.getDefaultInstance().getService();

  public static void blurOffensiveImages(JsonObject data) {
    String fileName = data.get("name").getAsString();
    String bucketName = data.get("bucket").getAsString();
    // Construct URI to GCS bucket and file.
    BlobInfo blobInfo = BlobInfo.newBuilder(bucketName, fileName).build();
// FIX THIS
    String gcsPath = String.format("gs://%s/%s", bucketName, fileName); //blobInfo.getSelfLink();
    System.out.println(String.format("Analyzing %s", fileName));

    // Construct request.
    List<AnnotateImageRequest> requests = new ArrayList<>();
    ImageSource imgSource = ImageSource.newBuilder().setImageUri(gcsPath).build();
    Image img = Image.newBuilder().setSource(imgSource).build();
    Feature feature = Feature.newBuilder().setType(Type.SAFE_SEARCH_DETECTION).build();
    AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(feature).setImage(img).build();
    requests.add(request);

    // Send request to the Vision API.
    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      BatchAnnotateImagesResponse response = client.batchAnnotateImages(requests);
      List<AnnotateImageResponse> responses = response.getResponsesList();
      for (AnnotateImageResponse res : responses) {
        if (res.hasError()) {
          System.out.println(String.format("Error: %s\n", res.getError().getMessage()));
          return;
        }
        // Get Safe Search Annotations
        SafeSearchAnnotation annotation = res.getSafeSearchAnnotation();
        if (annotation.getAdultValue() == 5 || annotation.getViolenceValue() == 5) {
          System.out.println(String.format("Detected %s as inappropriate.", fileName));
          blur(blobInfo);
        } else {
          System.out.println(String.format("Detected %s as OK.", fileName));
        }
      }
    } catch (Exception e) {
      System.out.println(e);
    }
  }

  public static void blur(BlobInfo blobInfo) throws IOException {
    String bucketName = blobInfo.getBucket();
    String fileName = blobInfo.getName();
    //Download image
    Blob blob = storage.get(BlobId.of(bucketName, fileName));
    String tmpFile = "/tmp/test.jpg";
    String tmpBlurFile = "/tmp/test-blur.jpg";
    Path download = Paths.get(tmpFile);
    Path upload = Paths.get(tmpBlurFile);
    blob.downloadTo(download);

    // Use - as output to use stdout.
    List<String> args = new ArrayList<String>();
    args.add("convert");
    args.add(tmpFile);
    args.add("-blur");
    args.add("0x8");
    args.add(tmpBlurFile);

    // StringBuilder output = new StringBuilder();
    // InputStream stdout = null;
    try {
      System.out.println("start");
      ProcessBuilder pb = new ProcessBuilder(args);
      Process process = pb.start();
      // OutputStream stdin = process.getOutputStream();
      // stdout = process.getInputStream();
      // // The Graphviz dot program reads from stdin.
      // Writer writer = new OutputStreamWriter(stdin, "UTF-8");
      // String content = new String(Files.readAllBytes(Paths.get(tmpFile)));
      // // BufferedReader reader = new BufferedReader(new FileReader(tmpFile));
      // // String line;
      // // while ((line = reader.readLine()) != null) {
      //   writer.write(content);
      // // }
      // // reader.close();
      // writer.close();
      process.waitFor();
      System.out.println("end");
    } catch (Exception e) {
      System.out.println("oops");
      System.out.println(e);
    }
    // System.out.println(stdout.available());
    // Upload image to blurred bucket.
    BlobId blurredBlobId = BlobId.of(BLURRED_BUCKET_NAME, fileName);
    BlobInfo blurredBlobInfo = BlobInfo.newBuilder(blurredBlobId).setContentType("image/jpg").build(); //SET TYPE!
    try {
      byte[] fileContent = Files.readAllBytes(upload);
      Blob blurredBlob = storage.create(blurredBlobInfo, fileContent);
      System.out.println(String.format("Blurred image uploaded to: gs://%s/%s", BLURRED_BUCKET_NAME, fileName));
    } catch (Exception e) {
      System.out.println("oops");
      System.out.println(e);
    }

    //remove image
  }
}
