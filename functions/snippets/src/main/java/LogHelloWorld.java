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

// [START functions_log_helloworld]
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class LogHelloWorld {

  private static final Logger LOGGER = Logger.getLogger(LogHelloWorld.class.getName());

  public void logHelloWorld(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    LOGGER.info("I am an info log!");
    LOGGER.warning("I am a warning log!");

    PrintWriter writer = response.getWriter();
    writer.write("Messages successfully logged!");
  }
}

// [END functions_log_helloworld]
