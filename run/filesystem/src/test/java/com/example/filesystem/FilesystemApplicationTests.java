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
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import org.junit.BeforeClass;
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

  private static String mntDir = System.getenv("MNT_DIR");
  String filename = System.getenv().getOrDefault("FILENAME", "Dockerfile");

  @BeforeClass
  public static void ensureEnvVar() {
    assertTrue("MNT_DIR env var must be defined.", System.getenv("MNT_DIR") != null);
  }

  @Test
  public void indexReturnsRedirect() throws Exception {
    mockMvc.perform(get("/")).andExpect(status().is3xxRedirection());
  }

  @Test
  public void pathReturnsRedirect() throws Exception {
    mockMvc.perform(get("/not/a/path")).andExpect(status().is3xxRedirection());
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

  /** Set Env Vars for testing purposes */
  @SuppressWarnings("unchecked")
  private static Map<String, String> getModifiableEnvironment() throws Exception {
    Class<?> pe = Class.forName("java.lang.ProcessEnvironment");
    Method getenv = pe.getDeclaredMethod("getenv");
    getenv.setAccessible(true);
    Object unmodifiableEnvironment = getenv.invoke(null);
    Class<?> map = Class.forName("java.util.Collections$UnmodifiableMap");
    Field m = map.getDeclaredField("m");
    m.setAccessible(true);
    return (Map<String, String>) m.get(unmodifiableEnvironment);
  }
}
