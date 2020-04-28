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

import com.google.cloud.functions.HttpRequest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MockHttpPart implements HttpRequest.HttpPart {
  private Optional<String> fileName;
  private InputStream inputStream = InputStream.nullInputStream();

  public void setFileName(String name) {
    fileName = Optional.of(name);
  }

  public void setInputStream(InputStream stream) {
    inputStream = stream;
  }

  @Override
  public Optional<String> getFileName() {
    return fileName;
  }

  @Override
  public InputStream getInputStream() {
    return inputStream;
  }

  // Auto-stubbed methods below
  @Override
  public Optional<String> getContentType() {
    return Optional.empty();
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
  public BufferedReader getReader() {
    return null;
  }

  @Override
  public Map<String, List<String>> getHeaders() {
    return null;
  }
}
