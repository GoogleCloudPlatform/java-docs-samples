/*
 * Copyright 2021 Google LLC
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

import jakarta.annotation.PreDestroy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.io.IOUtils;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerMapping;

@SpringBootApplication
public class FilesystemApplication {

  // Set config for file system path and filename prefix
  String mntDir = System.getenv().getOrDefault("MNT_DIR", "/mnt/nfs/filestore");
  String filename = System.getenv().getOrDefault("FILENAME", "test");

  @RestController
  /**
   * Redirects to the file system path to interact with file system
   * Writes a new file on each request
   */
  class FilesystemController {

    @GetMapping("/**")
    ResponseEntity<String> index(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
      // Retrieve URL path
      String path =
          (String) request.getAttribute(HandlerMapping.PATH_WITHIN_HANDLER_MAPPING_ATTRIBUTE);

      // Redirect to mount path
      if (!path.startsWith(mntDir)) {
        response.sendRedirect(mntDir);
      }

      String html = "<html><body>\n";
      if (!path.equals(mntDir)) {
        // Add parent mount path link
        html += String.format("<a href=\"%s\">%s</a><br/><br/>\n", mntDir, mntDir);
      } else {
        // Write a new test file
        try {
          writeFile(mntDir, filename);
        } catch (IOException e) {
          System.out.println("Error writing file: " + e.getMessage());
        }
      }

      // Return all files if path is a directory, else return the file
      File filePath = new File(path);
      if (filePath.isDirectory()) {
        File[] files = filePath.listFiles();
        for (File file : files) {
          html +=
              String.format("<a href=\"%s\">%s</a><br/>\n", file.getAbsolutePath(), file.getName());
        }
      } else {
        try {
          html += readFile(path);
        } catch (IOException e) {
          return ResponseEntity.status(HttpStatus.NOT_FOUND)
              .body("Error retrieving file: " + e.getMessage());
        }
      }

      html += "</body></html>\n";
      return ResponseEntity.status(HttpStatus.OK).body(html);
    }
  }

  public static void main(String[] args) {
    SpringApplication.run(FilesystemApplication.class, args);
  }

  /**
   * Write files to a directory with date created
   *
   * @param mntDir The path to the parent directory
   * @param filename The prefix filename
   * @throws IOException if the file can not be written
   */
  public static void writeFile(String mntDir, String filename) throws IOException {
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    DateFormat fileFormat = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
    Date date = new Date();

    String fileDate = fileFormat.format(date);
    String convertedFilename = String.format("%s-%s.txt", filename, fileDate);
    Path file = Paths.get(mntDir, convertedFilename);
    FileOutputStream outputStream = new FileOutputStream(file.toString());

    String message = "This test file was created on " + dateFormat.format(date);
    outputStream.write(message.getBytes());
    outputStream.close();
  }

  /**
   * Read files and return contents
   *
   * @param fullPath The path to the file
   * @return The file data
   * @throws IOException if the file does not exist
   */
  public static String readFile(String fullPath) throws IOException {
    FileInputStream inputStream = new FileInputStream(fullPath);
    String data = IOUtils.toString(inputStream, "UTF-8");
    return data;
  }

  /** Register shutdown hook */
  @PreDestroy
  public void tearDown() {
    System.out.println(FilesystemApplication.class.getSimpleName() + ": received SIGTERM.");
  }
}
