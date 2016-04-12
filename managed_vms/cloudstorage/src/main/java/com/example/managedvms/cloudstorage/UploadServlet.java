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

import com.google.cloud.storage.Acl;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    // Modify access list to allow all users with link to read file
    List<Acl> acls = new ArrayList<>();
    acls.add(Acl.of(Acl.User.ofAllUsers(), Acl.Role.READER));
    // the inputstream is closed by default, so we don't need to close it here
    Blob blob =
        storage.create(
            BlobInfo.builder(BUCKET_NAME, fileName).acl(acls).build(),
            filePart.getInputStream());

    // return the public download link
    resp.getWriter().print(blob.mediaLink());
  }
}
// [END example]
