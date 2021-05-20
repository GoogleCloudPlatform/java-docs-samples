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

package com.example.storagetransfer;

// [START cloudrun_tips_chunked_download]
// [START cloudrun_tips_chunked_upload]
// [START cloudrun_tips_storage_download]
// [START cloudrun_tips_storage_streaming]
// [START functions_tips_chunked_download]
// [START functions_tips_chunked_upload]
// [START functions_tips_storage_download]
// [START functions_tips_storage_streaming]

import com.google.cloud.ReadChannel;
import com.google.cloud.WriteChannel;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.UUID;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@SpringBootApplication
@EnableAsync
public class ExampleApplication {
  private static final String TARGET_BUCKET = System.getenv("TARGET_BUCKET");
  private static final Storage STORAGE =
      StorageOptions.newBuilder().build().getService();

  // Specify streaming transfer buffer size
  // Default is 64 kibibytes (1 kibibyte = 1024 bytes)
  private static final int BYTE_BUFFER_SIZE = 64 * 1024;

  // [END cloudrun_tips_chunked_download]
  // [END cloudrun_tips_chunked_upload]
  // [END cloudrun_tips_storage_download]
  // [END cloudrun_tips_storage_streaming]
  // [END functions_tips_chunked_download]
  // [END functions_tips_chunked_upload]
  // [END functions_tips_storage_download]
  // [END functions_tips_storage_streaming]

  // [START cloudrun_tips_storage_download]
  // [START functions_tips_storage_download]
  @RestController
  class NonStreamingCopyExampleController {

    @GetMapping("/non-streaming")
    String nonStreamingCopy(
        final @RequestParam(name = "suffix") Optional<String> suffixParameter
    ) throws IOException {
      // Add a suffix to the copied file to prevent filename collisions.
      String suffix = suffixParameter.orElse(UUID.randomUUID().toString());
      String targetName = String.format("puppies-copy-%s.jpg", suffix);

      Path tempFilePath = Paths.get(
          System.getProperty("java.io.tmpdir"),
          String.format("non-streaming-%s.jpg", suffix)
      );

      Blob downloadedBlob = STORAGE.get(
          BlobId.of("cloud-devrel-public", "puppies.jpg"));
      downloadedBlob.downloadTo(tempFilePath);

      BlobId blobId = BlobId.of(
          TARGET_BUCKET, String.format("puppies-copy-%s.jpg", suffix));
      BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
      STORAGE.create(blobInfo, Files.readAllBytes(tempFilePath));

      return String.format("Download-and-copy complete: %s", targetName);
    }
  }
  // [END cloudrun_tips_storage_download]
  // [END functions_tips_storage_download]

  // [START cloudrun_tips_storage_streaming]
  // [START functions_tips_storage_streaming]
  @RestController
  class StreamingCopyExampleController {

    @GetMapping("/streaming")
    String streamingCopy(
        final @RequestParam(name = "suffix") Optional<String> suffixParameter
    ) throws IOException {
      // Add a suffix to the copied file to prevent filename collisions.
      String suffix = suffixParameter.orElse(UUID.randomUUID().toString());
      String targetName =
          String.format("puppies-streaming-copy-%s.jpg", suffix);

      ReadChannel reader =
          STORAGE.reader("cloud-devrel-public", "puppies.jpg");
      ByteBuffer bytes = ByteBuffer.allocate(BYTE_BUFFER_SIZE);

      BlobId targetBlob = BlobId.of(
          TARGET_BUCKET,
          String.format("puppies-streaming-copy-%s.jpg", suffix)
      );
      BlobInfo blobInfo = BlobInfo.newBuilder(targetBlob)
          .setContentType("text/plain")
          .build();
      WriteChannel storageWriteChannel = STORAGE.writer(blobInfo);

      while (reader.read(bytes) > 0) {
        bytes.flip();
        storageWriteChannel.write(bytes);
        bytes.clear();
      }

      storageWriteChannel.close();

      return String.format("Streaming copy complete: %s", targetName);
    }
  }
  // [END cloudrun_tips_storage_streaming]
  // [END functions_tips_storage_streaming]

  // [START cloudrun_tips_chunked_download]
  // [START functions_tips_chunked_download]
  @RestController
  class HttpChunkedDownloadExampleController {

    @GetMapping("/download")
    public ResponseEntity<StreamingResponseBody> downloadChunkedHttp() {
      StreamingResponseBody body = output -> {
          ByteBuffer bytes = ByteBuffer.allocate(BYTE_BUFFER_SIZE);
          ReadChannel reader =
              STORAGE.reader("cloud-devrel-public", "data.txt");

          while (reader.read(bytes) > 0) {
            bytes.flip();
            output.write(bytes.array());
            bytes.clear();
          }
      };

      return ResponseEntity.ok().body(body);
    }
  }
  // [END cloudrun_tips_chunked_download]
  // [END functions_tips_chunked_download]

  // [START cloudrun_tips_chunked_upload]
  // [START functions_tips_chunked_upload]
  @RestController
  class HttpChunkedUploadExampleController {

    @PostMapping("/upload")
    String uploadChunkedHttp(
        final @RequestParam(name = "fileId") Optional<String> fileIdParameter,
        final @RequestParam("file") MultipartFile file
    ) throws IOException {
      // Add an ID to the uploaded filename to prevent filename collisions.
      String fileId = fileIdParameter.orElse(UUID.randomUUID().toString());

      InputStream inputStream = file.getInputStream();

      // Stream- the attached file to Cloud Storage
      String targetName = String.format(
          "chunked-http-%s-%s", fileId, file.getOriginalFilename());
      BlobId targetBlob = BlobId.of(TARGET_BUCKET, targetName);
      WriteChannel channel =
          STORAGE.writer(BlobInfo.newBuilder(targetBlob).build());

      while (inputStream.available() > 0) {
        ByteBuffer bytes = ByteBuffer.wrap(
            inputStream.readNBytes(BYTE_BUFFER_SIZE));
        channel.write(bytes);
      }

      channel.close();

      return String.format("Chunked upload complete: %s", targetName);
    }
  }
  // [END cloudrun_tips_chunked_upload]
  // [END functions_tips_chunked_upload]
  // (Not joining this and the parenthesis below for cross-sample consistency.)


  // [START cloudrun_tips_chunked_download]
  // [START cloudrun_tips_chunked_upload]
  // [START cloudrun_tips_storage_download]
  // [START cloudrun_tips_storage_streaming]
  // [START functions_tips_chunked_download]
  // [START functions_tips_chunked_upload]
  // [START functions_tips_storage_download]
  // [START functions_tips_storage_streaming]
}
// [END cloudrun_tips_chunked_download]
// [END cloudrun_tips_chunked_upload]
// [END cloudrun_tips_storage_download]
// [END cloudrun_tips_storage_streaming]
// [END functions_tips_chunked_download]
// [END functions_tips_chunked_upload]
// [END functions_tips_storage_download]
// [END functions_tips_storage_streaming]
