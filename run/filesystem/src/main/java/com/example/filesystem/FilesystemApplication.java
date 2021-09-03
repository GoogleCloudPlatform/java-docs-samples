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

import java.io.FileInputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

import javax.annotation.PreDestroy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.apache.commons.io.IOUtils;

@SpringBootApplication
public class FilesystemApplication {

  String mntDir = System.getenv().getOrDefault("MNT_DIR", "/mnt/nfs/filestore");
  String filename = System.getenv().getOrDefault("FILENAME", "testfile");

  @RestController
  class FilesystemController {
    @GetMapping("/")
    String index() {
      try {
        Path fullPath = Paths.get(mntDir, filename);
        FileInputStream inputStream = new FileInputStream(fullPath.toString());
        String data = IOUtils.toString(inputStream, "UTF-8");
        return data;
      } catch (Exception err) {
        return "Error retrieving file: " + err.toString();
      }
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(FilesystemApplication.class, args);
  }

  /** Register shutdown hook */
  @PreDestroy
  public void tearDown() {
    System.out.println(FilesystemApplication.class.getSimpleName() + ": received SIGTERM.");
  }
}
