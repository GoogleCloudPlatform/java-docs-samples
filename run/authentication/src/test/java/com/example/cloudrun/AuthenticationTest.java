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

package com.example.cloudrun;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.core.StringContains.containsString;

import com.google.api.client.http.HttpResponse;
import java.io.IOException;
import org.junit.Test;

public class AuthenticationTest {

  @Test
  public void canMakeGetRequest() throws IOException {
    String url = "https://example.com";
    HttpResponse response = Authentication.makeGetRequest(url, url);
    assertThat(response.parseAsString(), containsString("Example Domain"));
    assertThat(response.getContentType(), containsString("text/html"));
    assertThat(response.getStatusCode(), equalTo(200));
  }

  @Test
  public void failsMakeGetRequestWithoutProtocol() throws IOException {
    String url = "example.com/";
    try {
      Authentication.makeGetRequest(url, url);
    } catch (IllegalArgumentException e) {
      assertThat(e.getMessage(), containsString("no protocol"));
    }
  }
}
