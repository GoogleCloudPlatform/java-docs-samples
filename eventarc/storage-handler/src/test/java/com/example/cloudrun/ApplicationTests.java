/*
 * Copyright 2023 Google LLC
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

import static org.hamcrest.CoreMatchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.google.events.cloud.storage.v1.StorageObjectData;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.v1.CloudEventBuilder;
import io.cloudevents.spring.http.CloudEventHttpUtils;
import java.net.URI;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@AutoConfigureMockMvc
public class ApplicationTests {

  @Autowired MockMvc mockMvc;

  String mockBody;
  String jsondata;
  CloudEvent testevent;

  @Before
  public void setup() throws InvalidProtocolBufferException {
    StorageObjectData testdata =
        StorageObjectData.newBuilder().setBucket("testbucket").setName("test-file.txt").build();
    jsondata = JsonFormat.printer().print(testdata);
    testevent =
        new CloudEventBuilder()
            .withId("1")
            .withSource(URI.create("test"))
            .withSubject("testbucket")
            .withType("test")
            .withData("application/json", jsondata.getBytes())
            .build();
  }

  @Test
  public void testInvalidMimetype() throws Exception {
    mockMvc
        .perform(post("/").contentType(MediaType.TEXT_HTML).content(jsondata))
        .andExpect(status().isUnsupportedMediaType());
  }

  @Test
  public void withRequiredHeaders() throws Exception {
    HttpHeaders heads = CloudEventHttpUtils.toHttp(testevent);
    mockMvc
        .perform(post("/").headers(heads).content(jsondata))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("testbucket/test-file.txt")));
  }

  @Test
  public void missingRequiredHeaders() throws Exception {
    HttpHeaders badHeaders = CloudEventHttpUtils.toHttp(testevent);
    // remove a required field from the header object.
    badHeaders.remove("ce-type");
    mockMvc
        .perform(post("/").headers(badHeaders).content(jsondata))
        .andExpect(status().isBadRequest());
  }
}
