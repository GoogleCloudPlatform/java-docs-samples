//[START all]
/*
 * Copyright (c) 2012 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.google.api.client.sample.storage.appengine.serviceaccount;

import com.google.api.client.googleapis.extensions.appengine.auth.oauth2.AppIdentityCredential;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Google Cloud Storage Service Account App Engine sample.
 *
 * @author Marc Cohen
 */
public class StorageSample extends HttpServlet {

  private static final long serialVersionUID = 1L;

  private static final String GCS_URI = "http://commondatastorage.googleapis.com";

  /** Global configuration of Google Cloud Storage OAuth 2.0 scope. */
  private static final String STORAGE_SCOPE =
      "https://www.googleapis.com/auth/devstorage.read_write";

  /** Global instance of the HTTP transport. */
  private static final HttpTransport HTTP_TRANSPORT = new NetHttpTransport();

  /** Global instance of HTML reference to XSL style sheet. */
  String XSL = "\n<?xml-stylesheet href=\"/xsl/listing.xsl\" type=\"text/xsl\"?>\n";

  @Override
  protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

    try {
      AppIdentityCredential credential = new AppIdentityCredential(Arrays.asList(STORAGE_SCOPE));

      // Set up and execute Google Cloud Storage request.
      String bucketName = req.getRequestURI();
      if (bucketName.equals("/")) {
        resp.sendError(404, "No bucket specified - append /bucket-name to the URL and retry.");
        return;
      }
      // Remove any trailing slashes, if found.
      //[START snippet]
      String cleanBucketName = bucketName.replaceAll("/$", "");
      String URI = GCS_URI + cleanBucketName;
      HttpRequestFactory requestFactory = HTTP_TRANSPORT.createRequestFactory(credential);
      GenericUrl url = new GenericUrl(URI);
      HttpRequest request = requestFactory.buildGetRequest(url);
      HttpResponse response = request.execute();
      String content = response.parseAsString();
      //[END snippet]

      // Display the output XML.
      resp.setContentType("text/xml");
      BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(resp.getOutputStream()));
      String formattedContent = content.replaceAll("(<ListBucketResult)", XSL + "$1");
      writer.append(formattedContent);
      writer.flush();
      resp.setStatus(200);
    } catch (Throwable e) {
      resp.sendError(404, e.getMessage());
    }
  }
}
//[END all]
