/*
 * Copyright 2019 Google LLC
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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Base64;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class PubSubControllerTests {

  @Autowired private MockMvc mockMvc;

  @Test
  public void errorsWithEmptyBody() throws Exception {
    mockMvc.perform(post("/")).andExpect(status().isBadRequest());
  }

  @Test
  public void errorsWithNoMessage() throws Exception {
    mockMvc
        .perform(post("/").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void errorsWithInvalidMimetype() throws Exception {
    String mock = mockBody("{\"bucket\":\"test\",\"name\":\"test\"}");
    mockMvc
        .perform(post("/").contentType(MediaType.TEXT_HTML).content(mock))
        .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  public void errorsWithNoJsonBody() throws Exception {
    String mock = mockBody("not-JSON");
    mockMvc
        .perform(post("/").contentType(MediaType.APPLICATION_JSON).content(mock))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void errorsWithMissingData() throws Exception {
    String mock = mockBody("{\"json\":\"test\"}");
    mockMvc
        .perform(post("/").contentType(MediaType.APPLICATION_JSON).content(mock))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void errorsWithMissingName() throws Exception {
    String mock = mockBody("{\"name\":\"test\"}");
    mockMvc
        .perform(post("/").contentType(MediaType.APPLICATION_JSON).content(mock))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void errorsWithMissingBucket() throws Exception {
    String mock = mockBody("{\"name\":\"test\"}");
    mockMvc
        .perform(post("/").contentType(MediaType.APPLICATION_JSON).content(mock))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void addFullBody() throws Exception {
    String mock = mockBody("{\"bucket\":\"test\",\"name\":\"test\"}");
    mockMvc
        .perform(post("/").contentType(MediaType.APPLICATION_JSON).content(mock))
        .andExpect(status().isOk());
  }

  public static String mockBody(String body) {
    String encodedBody = Base64.getEncoder().encodeToString(body.getBytes());
    String mock =
        String.format(
            "{\"message\":{\"data\":\"%s\",\"attributes\":{},\"messageId\":\""
            + "91010751788941\",\"publishTime\":\"2017-09-25T23:16:42.302Z\"}}",
            encodedBody);
    return mock;
  }
}
