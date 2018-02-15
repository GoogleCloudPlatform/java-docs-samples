/*
 * Copyright 2016 Google Inc.
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

package com.example.compute.errorreporting;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import org.fluentd.logger.FluentLogger;

// [START example]
public class ExceptionUtil {
  private static FluentLogger ERRORS = FluentLogger.getLogger("myapp");

  public static void main(String[] args) {
    try {
      throw new Exception("Generic exception for testing Stackdriver");
    } catch (Exception e) {
      report(e);
    }
  }

  public static void report(Throwable ex) {
    StringWriter exceptionWriter = new StringWriter();
    ex.printStackTrace(new PrintWriter(exceptionWriter));
    Map<String, Object> data = new HashMap<>();
    data.put("message", exceptionWriter.toString());
    Map<String,String> serviceContextData = new HashMap<>();
    serviceContextData.put("service", "myapp");
    data.put("serviceContext", serviceContextData);
    // ... add more metadata
    ERRORS.log("errors", data);
  }
}
// [END example]
