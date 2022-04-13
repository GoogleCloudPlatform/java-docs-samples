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

// [START functions_helloworld_error]

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.IOException;
import java.util.logging.Logger;

public class HelloError implements HttpFunction {

  private static final Logger logger = Logger.getLogger(HelloError.class.getName());

  @Override
  public void service(HttpRequest request, HttpResponse response)
      throws IOException {
    // These will NOT be reported to Error Reporting
    System.err.println("I failed you");
    logger.severe("I failed you");

    // This WILL be reported to Error Reporting
    throw new RuntimeException("I failed you");
  }
}
// [END functions_helloworld_error]
