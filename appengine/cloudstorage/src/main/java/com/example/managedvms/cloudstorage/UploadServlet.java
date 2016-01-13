/**
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.example.managedvms.cloudstorage;

import com.google.common.io.ByteStreams;
import com.google.gcloud.storage.BlobInfo;
import com.google.gcloud.storage.BlobWriteChannel;
import com.google.gcloud.storage.Storage;
import com.google.gcloud.storage.StorageOptions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.Channels;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

// [START example]
@SuppressWarnings("serial")
@WebServlet(name = "upload", value = "/upload")
@MultipartConfig()
public class UploadServlet extends HttpServlet {

  private static final String BUCKET_NAME = System.getenv("BUCKET_NAME");
  private static Storage storage = null;

  @Override
  public void init() {
    storage = StorageOptions.defaultInstance().service();
  }

  @Override
  public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException,
      ServletException {
    final Part filePart = req.getPart("file");
    final String fileName = filePart.getSubmittedFileName();
    BlobInfo blobInfo = BlobInfo.builder(BUCKET_NAME, fileName).build();
    InputStream filecontent = filePart.getInputStream();
    BlobWriteChannel blobWriter = storage.writer(blobInfo);
    ByteStreams.copy(filecontent, Channels.newOutputStream(blobWriter));
    blobWriter.close();
    blobInfo = storage.get(BUCKET_NAME, fileName);
    resp.getWriter().print(blobInfo.mediaLink());
  }
}
// [END example]
