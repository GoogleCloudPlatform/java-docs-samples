/*
 * Copyright 2020 Google LLC
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

package functions;

// [START functions_http_method]

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.net.HttpURLConnection;

public class HttpMethod implements HttpFunction {
  @Override
  public void service(HttpRequest request, HttpResponse response)
      throws IOException {

    BufferedWriter writer = response.getWriter();

    switch (request.getMethod()) {
      case "GET":
        response.setStatusCode(HttpURLConnection.HTTP_OK);
        writer.write("Hello world!");
        break;
      case "PUT":
        response.setStatusCode(HttpURLConnection.HTTP_FORBIDDEN);
        writer.write("Forbidden!");
        break;
      default:
        response.setStatusCode(HttpURLConnection.HTTP_BAD_METHOD);
        writer.write("Something blew up!");
        break;
    }
  }
}
// [END functions_http_method]
