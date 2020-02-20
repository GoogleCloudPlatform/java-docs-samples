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

import com.google.cloud.functions.HttpRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class MockHttpRequest implements HttpRequest {

  public BufferedReader bufferedReader;
  public HashMap<String, List<String>> queryParams = new HashMap<>();
  public Optional<String> contentType = Optional.empty();
  public InputStream inputStream;
  public String httpMethod;

  @Override
  public Optional<String> getFirstQueryParameter(String name) {
    if (queryParams.containsKey(name)) {
      return Optional.of(queryParams.get(name).get(0));
    } else {
      return Optional.empty();
    }
  }

  @Override
  public BufferedReader getReader() throws IOException {
    return bufferedReader;
  }

  @Override
  public Optional<String> getContentType() {
    return contentType;
  }

  @Override
  public InputStream getInputStream() throws IOException {
    return inputStream;
  }

  @Override
  public String getMethod() {
    return httpMethod;
  }

  // Unused (but compiler-required) methods below
  @Override
  public Map<String, HttpPart> getParts() {
    return null;
  }

  @Override
  public String getUri() {
    return null;
  }

  @Override
  public String getPath() {
    return null;
  }

  @Override
  public Optional<String> getQuery() {
    return Optional.empty();
  }

  @Override
  public Map<String, List<String>> getQueryParameters() {
    return null;
  }

  @Override
  public long getContentLength() {
    return 0;
  }

  @Override
  public Optional<String> getCharacterEncoding() {
    return Optional.empty();
  }

  @Override
  public Map<String, List<String>> getHeaders() {
    return null;
  }
}
