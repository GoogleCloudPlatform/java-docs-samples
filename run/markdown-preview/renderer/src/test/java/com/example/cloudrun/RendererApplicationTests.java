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

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@ContextConfiguration(classes = RendererApplication.class)
@WebMvcTest(MarkdownController.class)
class RendererApplicationTests {

  @Autowired private MockMvc mockMvc;

  @Test
  public void postMarkdown() throws Exception {
    Map<String, String> markdown = new HashMap<String, String>();
    markdown.put("input", "**strong text**");
    markdown.put("want", "<p><strong>strong text</strong></p>\n");
    Map<String, String> sanitize = new HashMap<String, String>();
    sanitize.put("input", "<a onblur=\"alert(secret)\" href=\"http://www.google.com\">Google</a>");
    sanitize.put("want", "<p><a href=\"http://www.google.com\" rel=\"nofollow\">Google</a></p>");

    this.mockMvc
        .perform(post("/").contentType(MediaType.APPLICATION_JSON).content(markdown.get("input")))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(markdown.get("want"))));

    this.mockMvc
        .perform(post("/").contentType(MediaType.APPLICATION_JSON).content(sanitize.get("input")))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(sanitize.get("want"))));
  }

  @Test
  public void failsGet() throws Exception {
    this.mockMvc.perform(get("/")).andExpect(status().isMethodNotAllowed());
  }
}
