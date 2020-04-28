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

package functions;

// [START functions_concepts_filesystem]

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

public class FileSystem implements HttpFunction {

  // Lists the files in the current directory.
  @Override
  public void service(HttpRequest request, HttpResponse response)
      throws IOException {
    File currentDirectory = new File(".");
    File[] files = currentDirectory.listFiles();
    PrintWriter writer = new PrintWriter(response.getWriter());
    writer.println("Files:");
    for (File f : files) {
      writer.printf("\t%s%n", f.getName());
    }
  }
}
// [END functions_concepts_filesystem]
