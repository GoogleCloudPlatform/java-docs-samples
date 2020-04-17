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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EditorController {

  private static final Logger logger = LoggerFactory.getLogger(EditorController.class);

  @GetMapping("/")
  public String getIndex(Model model) {
    String defaultContent = loadMarkdown("templates/markdown.md");
    model.addAttribute("Default", defaultContent);
    return "index";
  }

  public String loadMarkdown(String filename) {
    String text = "";
    ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    try {
      File file = new File(classLoader.getResource(filename).getFile());
      text = new String(Files.readAllBytes(file.toPath()));
    } catch (IOException e) {
      logger.error("Unable to load file " + filename, e);
    }
    return text;
  }
}
