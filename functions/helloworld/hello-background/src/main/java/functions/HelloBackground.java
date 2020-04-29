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

// [START functions_helloworld_background]

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.cloud.functions.HttpRequest;
import java.util.logging.Logger;

public class HelloBackground implements BackgroundFunction<HttpRequest> {
  private static final Logger logger = Logger.getLogger(HelloBackground.class.getName());

  @Override
  public void accept(HttpRequest request, Context context) {
    // name's default value is "world"
    String name = request.getFirstQueryParameter("name").orElse("world");
    logger.info(String.format("Hello %s!", name));
  }
}
// [END functions_helloworld_background]
