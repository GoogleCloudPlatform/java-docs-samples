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

// [START functions_tips_scopes]
// [START cloudrun_tips_global_scope]
// [START run_tips_global_scope]

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class Scopes implements HttpFunction {
  // Global (instance-wide) scope
  // This computation runs at instance cold-start.
  // Warning: Class variables used in functions code must be thread-safe.
  private static final int INSTANCE_VAR = heavyComputation();

  @Override
  public void service(HttpRequest request, HttpResponse response)
      throws IOException {
    // Per-function scope
    // This computation runs every time this function is called
    int functionVar = lightComputation();

    var writer = new PrintWriter(response.getWriter());
    writer.printf("Instance: %s; function: %s", INSTANCE_VAR, functionVar);
  }

  private static int lightComputation() {
    int[] numbers = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    return Arrays.stream(numbers).sum();
  }

  private static int heavyComputation() {
    int[] numbers = new int[] { 1, 2, 3, 4, 5, 6, 7, 8, 9 };
    return Arrays.stream(numbers).reduce((t, x) -> t * x).getAsInt();
  }
}
// [END run_tips_global_scope]
// [END cloudrun_tips_global_scope]
// [END functions_tips_scopes]
