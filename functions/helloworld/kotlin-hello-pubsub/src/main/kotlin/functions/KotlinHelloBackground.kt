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
import com.google.cloud.functions.BackgroundFunction
import com.google.cloud.functions.Context
import com.google.events.cloud.pubsub.v1.Message;
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.logging.Logger


class HelloPubSub : BackgroundFunction<Message> {
    override fun accept(message: Message, context: Context) {
        // name's default value is "world"
        var name = "world"
        if (message?.data != null) {
            name = String(
                    Base64.getDecoder().decode(message.data!!.toByteArray(StandardCharsets.UTF_8)),
                    StandardCharsets.UTF_8)
        }
        LOGGER.info(String.format("Hello %s!", name))
        return;
    }

    companion object {
        private val LOGGER = Logger.getLogger(HelloPubSub::class.java.name)
    }
}
// [END functions_helloworld_pubsub]
