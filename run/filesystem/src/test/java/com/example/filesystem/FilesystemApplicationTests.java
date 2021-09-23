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

package com.example.filesystem;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class FilesystemApplicationTests {

  @Autowired private MockMvc mockMvc;

  String mntDir = System.getenv().getOrDefault("MNT_DIR", "/mnt/nfs/filestore");
  String filename = System.getenv().getOrDefault("FILENAME", "Dockerfile");

  @Test
  public void indexReturnsRedirect() throws Exception {
    mockMvc
        .perform(get("/"))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  public void pathReturnsRedirect() throws Exception {
    mockMvc
        .perform(get("/not/a/path"))
        .andExpect(status().is3xxRedirection());
  }

  @Test
  public void pathReturnsMnt() throws Exception {
    mockMvc
        .perform(get(mntDir))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString(filename)));
  }

  @Test
  public void pathReturnsFile() throws Exception {
    mockMvc
        .perform(get(mntDir + "/" + filename))
        .andExpect(status().isOk())
        .andExpect(content().string(containsString("ENTRYPOINT")));
  }

  @Test
  public void pathReturnsFileError() throws Exception {
    mockMvc
        .perform(get(mntDir + "/" + "notafile"))
        .andExpect(status().isNotFound())
        .andExpect(content().string(containsString("Error retrieving file")));
  }
}
