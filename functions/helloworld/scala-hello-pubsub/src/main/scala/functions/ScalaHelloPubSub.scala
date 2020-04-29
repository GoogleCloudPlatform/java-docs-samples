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

package functions

// [START functions_helloworld_pubsub]
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.logging.Logger

import com.google.cloud.functions.{BackgroundFunction, Context}
import functions.eventpojos.PubSubMessage

class ScalaHelloPubSub extends BackgroundFunction[PubSubMessage] {

  val LOGGER = Logger.getLogger(this.getClass.getName)

  override def accept(message: PubSubMessage, context: Context): Unit = {
    // name's default value is "world"
    var name = "world"
    if (message != null && message.getData != null) {
      name = new String(Base64.getDecoder.decode(
        message.getData.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8)
    }
    LOGGER.info(String.format("Hello %s!", name))
  }
}
// [END functions_helloworld_pubsub]
