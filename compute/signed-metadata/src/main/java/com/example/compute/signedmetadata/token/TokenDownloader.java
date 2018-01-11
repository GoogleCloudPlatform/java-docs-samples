/*
 * Copyright 2017 Google Inc.
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

package com.example.compute.signedmetadata.token;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;

public class TokenDownloader {

  private static final String HTTP_SCHEME = "http";
  private static final String NO_USER_INFO = null;
  private static final String METADATA_HOST_ADDRESS = "metadata";
  private static final int UNSPECIFIED_PORT = -1;
  private static final String METADATA_PATH = "/computeMetadata/v1/instance/"
      + "service-accounts/default/identity";
  private static final String FRAGMENT = null;

  public String getTokenWithAudience(String audience) throws URISyntaxException, IOException {
    String query = "audience=" + audience + "&format=full";
    URL website = new URI(HTTP_SCHEME, NO_USER_INFO, METADATA_HOST_ADDRESS, UNSPECIFIED_PORT,
        METADATA_PATH, query, FRAGMENT).toURL();
    URLConnection connection = website.openConnection();
    HttpURLConnection httpConnection = safelyCastToHttpUrlConnection(connection);
    httpConnection.setRequestProperty("Metadata-Flavor", "Google");
    return new Downloader().download(httpConnection);
  }

  private HttpURLConnection safelyCastToHttpUrlConnection(URLConnection connection) {
    if (connection instanceof HttpURLConnection) {
      return (HttpURLConnection) connection;
    } else {
      throw new RuntimeException("We do not have Http connection, but we used http schema");
    }
  }
}
