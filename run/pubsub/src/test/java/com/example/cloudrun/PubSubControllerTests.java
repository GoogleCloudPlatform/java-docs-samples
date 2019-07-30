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
  public void addEmptyBody() throws Exception {
    mockMvc.perform(post("/")).andExpect(status().isBadRequest());
  }

  @Test
  public void addNoMessage() throws Exception {
    String mockBody = "{}";

    mockMvc
        .perform(post("/").contentType(MediaType.APPLICATION_JSON).content(mockBody))
        .andExpect(status().isBadRequest());
  }

  @Test
  public void addInvalidMimetype() throws Exception {
    String mockBody = "{\"message\":{\"data\":\"dGVzdA==\","
                      + "\"attributes\":{},\"messageId\":\"91010751788941\""
                      + ",\"publishTime\":\"2017-09-25T23:16:42.302Z\"}}";

    mockMvc
        .perform(post("/").contentType(MediaType.TEXT_HTML).content(mockBody))
        .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  public void addMinimalBody() throws Exception {
    String mockBody = "{\"message\":{}}";

    mockMvc
        .perform(post("/").contentType(MediaType.APPLICATION_JSON).content(mockBody))
        .andExpect(status().isOk());
  }

  @Test
  public void addFullBody() throws Exception {
    String mockBody = "{\"message\":{\"data\":\"dGVzdA==\","
                      + "\"attributes\":{},\"messageId\":\"91010751788941\""
                      + ",\"publishTime\":\"2017-09-25T23:16:42.302Z\"}}";
    mockMvc
        .perform(post("/").contentType(MediaType.APPLICATION_JSON).content(mockBody))
        .andExpect(status().isOk());
  }
}
