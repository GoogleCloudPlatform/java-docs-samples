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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
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
public class EventControllerTests {

  @Autowired private MockMvc mockMvc;
  String mockBody;

  @Before
  public void setup() throws JSONException {
    JSONObject message =
        new JSONObject()
            .put("data", "dGVzdA==")
            .put("messageId", "91010751788941")
            .put("publishTime", "2017-09-25T23:16:42.302Z")
            .put("attributes", new JSONObject());
    mockBody = new JSONObject().put("message", message).toString();
  }

  @Test
  public void addEmptyBody() throws Exception {
    mockMvc.perform(post("/")).andExpect(status().isBadRequest());
  }

  @Test
  public void addNoMessage() throws Exception {
    mockMvc
        .perform(post("/").contentType(MediaType.APPLICATION_JSON).content("{}"))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void addInvalidMimetype() throws Exception {
    mockMvc
        .perform(post("/").contentType(MediaType.TEXT_HTML).content(mockBody))
        .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  public void addMinimalBody() throws Exception {
    String minimalMockBody = new JSONObject().put("message", new JSONObject()).toString();

    mockMvc
        .perform(post("/").contentType(MediaType.APPLICATION_JSON).content(minimalMockBody))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void addFullBody() throws Exception {
    mockMvc
        .perform(post("/").contentType(MediaType.APPLICATION_JSON).content(mockBody))
        .andExpect(status().isOk());
  }
}
