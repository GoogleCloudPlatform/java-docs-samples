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

// [START functions_tips_scopes]
// [START run_tips_global_scope]

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class Tips {
  // Global (instance-wide) scope
  // This computation runs at instance cold-start
  public static final int INSTANCE_VAR = heavyComputation();

  public void scopesDemo(HttpServletRequest request, HttpServletResponse response)
      throws IOException, InterruptedException {
    // Per-function scope
    // This computation runs every time this function is called
    final int functionVar = lightComputation();
    PrintWriter writer = response.getWriter();
    writer.write(String.format("Per instance: %d, per function: %d", INSTANCE_VAR, functionVar));
  }
  // [END functions_tips_scopes]
  // [END run_tips_global_scope]

  private static int lightComputation() {
    List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    return numbers.stream().reduce((t, x) -> t + x).get();
  }

  private static int heavyComputation() {
    List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9);
    return numbers.stream().reduce((t, x) -> t * x).get();
  }

  // [START functions_tips_scopes]
  // [START run_tips_global_scope]
}

// [END functions_tips_scopes]
// [END run_tips_global_scope]
  