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

// [START functions_concepts_stateless]

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutionCount implements HttpFunction {
  
  private final AtomicInteger count = new AtomicInteger(0);

  @Override
  public void service(HttpRequest request, HttpResponse response)
      throws IOException {
    count.getAndIncrement();

    // Note: the total function invocation count across
    // all instances may not be equal to this value!
    BufferedWriter writer = response.getWriter();
    writer.write("Instance execution count: " + count);
  }
}
// [END functions_concepts_stateless]
