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

// [START functions_log_helloworld]

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.logging.Logger;

public class LogHelloWorld implements HttpFunction {

  private static final Logger logger = Logger.getLogger(LogHelloWorld.class.getName());

  @Override
  public void service(HttpRequest request, HttpResponse response)
      throws IOException {
    System.out.println("I am a log to stdout!");
    System.err.println("I am a log to stderr!");

    logger.info("I am an info log!");
    logger.warning("I am a warning log!");

    BufferedWriter writer = response.getWriter();
    writer.write("Messages successfully logged!");
  }
}
// [END functions_log_helloworld]
