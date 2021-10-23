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

// [START functions_tips_lazy_globals]
// [START cloudrun_tips_global_lazy]
// [START run_tips_global_lazy]

import com.google.cloud.functions.HttpFunction;
import com.google.cloud.functions.HttpRequest;
import com.google.cloud.functions.HttpResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;

public class LazyFields implements HttpFunction {
  // Always initialized (at cold-start)
  // Warning: Class variables used in Servlet classes must be thread-safe,
  // or else might introduce race conditions in your code.
  private static final int NON_LAZY_GLOBAL = fileWideComputation();

  // Declared at cold-start, but only initialized if/when the function executes
  // Uses the "initialization-on-demand holder" idiom
  // More information: https://en.wikipedia.org/wiki/Initialization-on-demand_holder_idiom
  private static class LazyGlobalHolder {
    // Making the default constructor private prohibits instantiation of this class
    private LazyGlobalHolder() {}

    // This value is initialized only if (and when) the getLazyGlobal() function below is called
    private static final Integer INSTANCE = functionSpecificComputation();

    private static Integer getInstance() {
      return LazyGlobalHolder.INSTANCE;
    }
  }

  @Override
  public void service(HttpRequest request, HttpResponse response)
      throws IOException {
    Integer lazyGlobal = LazyGlobalHolder.getInstance();

    var writer = new PrintWriter(response.getWriter());
    writer.printf("Lazy global: %s; non-lazy global: %s%n", lazyGlobal, NON_LAZY_GLOBAL);
  }

  private static int functionSpecificComputation() {
    int[] numbers = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9};
    return Arrays.stream(numbers).sum();
  }

  private static int fileWideComputation() {
    int[] numbers = new int[] {1, 2, 3, 4, 5, 6, 7, 8, 9};
    return Arrays.stream(numbers).reduce((t, x) -> t * x).getAsInt();
  }
}
// [END run_tips_global_lazy]
// [END cloudrun_tips_global_lazy]
// [END functions_tips_lazy_globals]
