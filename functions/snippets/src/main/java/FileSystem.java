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

// [START functions_concepts_filesystem]

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;

public class FileSystem implements HttpFunction {

  // Lists the files in the current directory.
  @Override
  public void service(HttpRequest request, HttpResponse response)
      throws IOException {
    File currentDirectory = new File(".");
    File[] files = currentDirectory.listFiles();
    BufferedWriter writer = response.getWriter();
    writer.write("Files: \n");
    for (File f : files) {
      writer.write(String.format("\t%s\n", f.getName()));
    }
  }
}
// [END functions_concepts_filesystem]
