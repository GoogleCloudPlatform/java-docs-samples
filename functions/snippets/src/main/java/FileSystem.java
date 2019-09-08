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

// [START functions_concepts_filesystem]
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.nio.file.Files;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class FileSystem {

  // Lists the files in the current directory.
  public void listFiles(HttpServletRequest request, HttpServletResponse response)
      throws IOException {

    String currentDirectory = System.getProperty("user.dir");
    Path path = Paths.get(currentDirectory, "snippets/src/main/java/");
    try (Stream<Path> walk = Files.walk(path)) {
      List<String> fileList = walk.filter(Files::isRegularFile)
        .map(file -> file.toString()).collect(Collectors.toList());
      String files = String.join(", ", fileList);
      PrintWriter writer = response.getWriter();
      writer.write(String.format("Files: %s", files));
    };
  }
}

// [END functions_concepts_filesystem]
