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

package com.example.vision;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.Storage.BlobListOption;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.vision.v1p2beta1.AnnotateFileResponse;
import com.google.cloud.vision.v1p2beta1.AnnotateImageResponse;
import com.google.cloud.vision.v1p2beta1.AsyncAnnotateFileRequest;
import com.google.cloud.vision.v1p2beta1.AsyncAnnotateFileResponse;
import com.google.cloud.vision.v1p2beta1.AsyncBatchAnnotateFilesResponse;
import com.google.cloud.vision.v1p2beta1.Feature;
import com.google.cloud.vision.v1p2beta1.Feature.Type;
import com.google.cloud.vision.v1p2beta1.GcsDestination;
import com.google.cloud.vision.v1p2beta1.GcsSource;
import com.google.cloud.vision.v1p2beta1.ImageAnnotatorClient;
import com.google.cloud.vision.v1p2beta1.InputConfig;
import com.google.cloud.vision.v1p2beta1.OperationMetadata;
import com.google.cloud.vision.v1p2beta1.OutputConfig;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Detect {

  /**
   * Performs OCR detection on PDF/TIFF documents on Cloud Storage using the Vision API.
   *
   * @throws Exception on errors while closing the client.
   * @throws IOException on Input/Output errors.
   */
  public static void main(String[] args) throws Exception {
    argsHelper(args, System.out);
  }

  /**
   * Helper that handles the input passed to the program.
   *
   * @throws Exception on errors while closing the client.
   * @throws IOException on Input/Output errors.
   */
  public static void argsHelper(String[] args, PrintStream out) throws Exception {
    if (args.length < 1) {
      out.println("Usage:");
      out.printf(
          "\tmvn exec:java -DDetect -Dexec.args=\"ocr <path-to-file> <path-to-destination>\"\n"
              + "Commands:\n"
              + "\tocr\n"
              + "Path to File:\n\tA path to the remote file on Cloud Storage (gs://...)\n"
              + "Path to Destination\n\tA path to the remote destination on Cloud Storage for the"
              + " file to be saved. (gs://BUCKET_NAME/PREFIX/)\n");
      return;
    }
    String command = args[0];
    String path = args.length > 1 ? args[1] : "";

    if (command.equals("ocr")) {
      String destPath = args.length > 2 ? args[2] : "";
      detectDocumentsGcs(path, destPath);
    }
  }

  // [START vision_async_detect_document_ocr]
  /**
   * Performs document text OCR with PDF/TIFF as source files on Google Cloud Storage.
   *
   * @param gcsSourcePath The path to the remote file on Google Cloud Storage to detect document
   *                      text on.
   * @param gcsDestinationPath The path to the remote file on Google Cloud Storage to store the
   *                           results on.
   * @throws Exception on errors while closing the client.
   */
  public static void detectDocumentsGcs(String gcsSourcePath, String gcsDestinationPath) throws
      Exception {
    try (ImageAnnotatorClient client = ImageAnnotatorClient.create()) {
      List<AsyncAnnotateFileRequest> requests = new ArrayList<>();

      // Set the GCS source path for the remote file.
      GcsSource gcsSource = GcsSource.newBuilder()
          .setUri(gcsSourcePath)
          .build();

      // Create the configuration with the specified MIME (Multipurpose Internet Mail Extensions)
      // types
      InputConfig inputConfig = InputConfig.newBuilder()
          .setMimeType("application/pdf") // Supported MimeTypes: "application/pdf", "image/tiff"
          .setGcsSource(gcsSource)
          .build();

      // Set the GCS destination path for where to save the results.
      GcsDestination gcsDestination = GcsDestination.newBuilder()
          .setUri(gcsDestinationPath)
          .build();

      // Create the configuration for the output with the batch size.
      // The batch size sets how many pages should be grouped into each json output file.
      OutputConfig outputConfig = OutputConfig.newBuilder()
          .setBatchSize(2)
          .setGcsDestination(gcsDestination)
          .build();

      // Select the Feature required by the vision API
      Feature feature = Feature.newBuilder().setType(Type.DOCUMENT_TEXT_DETECTION).build();

      // Build the OCR request
      AsyncAnnotateFileRequest request = AsyncAnnotateFileRequest.newBuilder()
          .addFeatures(feature)
          .setInputConfig(inputConfig)
          .setOutputConfig(outputConfig)
          .build();

      requests.add(request);

      // Perform the OCR request
      OperationFuture<AsyncBatchAnnotateFilesResponse, OperationMetadata> response =
          client.asyncBatchAnnotateFilesAsync(requests);

      System.out.println("Waiting for the operation to finish.");

      // Wait for the request to finish. (The result is not used, since the API saves the result to
      // the specified location on GCS.)
      List<AsyncAnnotateFileResponse> result = response.get(90, TimeUnit.SECONDS)
          .getResponsesList();

      // Once the request has completed and the output has been
      // written to GCS, we can list all the output files.
      Storage storage = StorageOptions.getDefaultInstance().getService();

      // Get the destination location from the gcsDestinationPath
      Pattern pattern = Pattern.compile("gs://([^/]+)/(.+)");
      Matcher matcher = pattern.matcher(gcsDestinationPath);

      if (matcher.find()) {
        String bucketName = matcher.group(1);
        String prefix = matcher.group(2);

        // Get the list of objects with the given prefix from the GCS bucket
        Bucket bucket = storage.get(bucketName);
        com.google.api.gax.paging.Page<Blob> pageList = bucket.list(BlobListOption.prefix(prefix));

        Blob firstOutputFile = null;

        // List objects with the given prefix.
        System.out.println("Output files:");
        for (Blob blob : pageList.iterateAll()) {
          System.out.println(blob.getName());

          // Process the first output file from GCS.
          // Since we specified batch size = 2, the first response contains
          // the first two pages of the input file.
          if (firstOutputFile == null) {
            firstOutputFile = blob;
          }
        }

        // Get the contents of the file and convert the JSON contents to an AnnotateFileResponse
        // object. If the Blob is small read all its content in one request
        // (Note: the file is a .json file)
        // Storage guide: https://cloud.google.com/storage/docs/downloading-objects
        AnnotateFileResponse annotateFileResponse = AnnotateFileResponse.newBuilder()
            .mergeFrom(firstOutputFile.getContent())
            .build();

        // Parse through the object to get the actual response for the first page of the input file.
        AnnotateImageResponse annotateImageResponse = annotateFileResponse.getResponses(0);

        // Here we print the full text from the first page.
        // The response contains more information:
        // annotation/pages/blocks/paragraphs/words/symbols
        // including confidence score and bounding boxes
        System.out.format("\nText: %s\n", annotateImageResponse.getFullTextAnnotation());
      } else {
        System.out.println("No MATCH");
      }
    }
  }
  // [END vision_async_detect_document_ocr]
}
