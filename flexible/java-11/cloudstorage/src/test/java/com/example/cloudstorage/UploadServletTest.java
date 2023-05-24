/*
 * Copyright 2023 Google LLC
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

package com.example.cloudstorage;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import org.junit.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class UploadServletTest {

  @Test
  public void testPost() throws Exception {

    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);
    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    try (BufferedReader reader = mock(BufferedReader.class)) {
      when(request.getReader()).thenReturn(reader);
    }
    Part filePart = mock(Part.class);
    when(filePart.getSubmittedFileName()).thenReturn("testfile.txt");
    when(filePart.getInputStream()).thenReturn(mock(InputStream.class));
    when(request.getPart("file")).thenReturn(filePart);
    Storage mockStorage = mock(Storage.class);
    Blob mockBlob = mock(Blob.class);
    when(mockBlob.getMediaLink()).thenReturn("test blob data");
    when(mockStorage.create(any(BlobInfo.class), any(InputStream.class))).thenReturn(mockBlob);

    MockedStatic<StorageOptions> storageOptionsMock =
        Mockito.mockStatic(StorageOptions.class, Mockito.RETURNS_DEEP_STUBS);
    storageOptionsMock
        .when(() -> StorageOptions.getDefaultInstance().getService())
        .thenReturn(mockStorage);
    UploadServlet servlet = new UploadServlet();

    servlet.doPost(request, response);
    assertTrue(stringWriter.toString().contains("test blob data"));
  }
}
