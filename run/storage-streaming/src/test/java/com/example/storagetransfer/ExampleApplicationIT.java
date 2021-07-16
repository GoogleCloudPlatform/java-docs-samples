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

import static com.google.common.truth.Truth.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.File;
import java.io.FileInputStream;
import java.util.UUID;
import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class ExampleApplicationIT {
  // TODO<developer>: set this to the name of a bucket you own
  private static final String FUNCTIONS_BUCKET = System.getenv("TARGET_BUCKET");
  private static final Storage STORAGE = StorageOptions.newBuilder().build().getService();

  @Autowired
  private MockMvc mockMvc;

  @Test
  public void shouldNonStreamCopyFile() throws Exception {
    String suffix = UUID.randomUUID().toString();
    String expectedFile = String.format("puppies-copy-%s.jpg", suffix);
    String expectedResponse = "Download-and-copy complete: " + expectedFile;

    mockMvc
        .perform(get("/non-streaming").param("suffix", suffix))
        .andExpect(status().isOk())
        .andExpect(content().string(expectedResponse));

    Blob resultBlob = STORAGE.get(BlobId.of(FUNCTIONS_BUCKET, expectedFile));
    assertThat(resultBlob).isNotNull();
  }

  @Test
  public void shouldStreamCopyFile() throws Exception {
    String suffix = UUID.randomUUID().toString();
    String expectedFile = String.format("puppies-streaming-copy-%s.jpg", suffix);
    String expectedResponse = "Streaming copy complete: " + expectedFile;

    mockMvc
        .perform(get("/streaming").param("suffix", suffix))
        .andExpect(status().isOk())
        .andExpect(content().string(expectedResponse));

    Blob resultBlob = STORAGE.get(BlobId.of(FUNCTIONS_BUCKET, expectedFile));
    assertThat(resultBlob).isNotNull();
  }

  @Test
  public void shouldDownloadChunkedFile() throws Exception {
    MvcResult result = mockMvc
        .perform(get("/download"))
        .andDo((x) -> {
          // Wait for download to complete
          Thread.sleep(1000);
        })
        .andExpect(status().isOk())
        .andExpect(content().string(Matchers.containsString("hello world!")))
        .andReturn();
  }

  @Test
  public void shouldUploadChunkedFile() throws Exception {
    String fileId = UUID.randomUUID().toString();
    String expectedFile = String.format("chunked-http-%s-cat.jpg", fileId);
    String expectedResponse = "Chunked upload complete: " + expectedFile;

    FileInputStream file = new FileInputStream(new File("resources/cat.jpg"));

    MockMultipartFile multipartFile = new MockMultipartFile(
        "file", "cat.jpg", MediaType.IMAGE_JPEG_VALUE, file);

    mockMvc
        .perform(
            multipart("/upload")
                .file(multipartFile)
                .param("fileId", fileId)
        )
        .andExpect(status().isOk())
        .andExpect(content().string(expectedResponse));

    Blob resultBlob = STORAGE.get(BlobId.of(FUNCTIONS_BUCKET, expectedFile));
    assertThat(resultBlob).isNotNull();
  }
}
