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

import com.google.cloud.functions.HttpResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.Optional;

class MockHttpResponse implements HttpResponse {

  public BufferedWriter writer;

  @Override
  public BufferedWriter getWriter() throws IOException {
    return writer;
  }

  @Override
  public void setStatusCode(int i) {

  }

  @Override
  public void setStatusCode(int i, String s) {

  }

  @Override
  public void setContentType(String s) {

  }

  @Override
  public Optional<String> getContentType() {
    return Optional.empty();
  }

  @Override
  public void appendHeader(String s, String s1) {

  }

  @Override
  public Map<String, List<String>> getHeaders() {
    return null;
  }

  @Override
  public OutputStream getOutputStream() throws IOException {
    return null;
  }
}
