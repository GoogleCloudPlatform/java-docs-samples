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
package com.example.functions

// [START functions_helloworld_background]
import com.google.cloud.functions.BackgroundFunction
import com.google.cloud.functions.Context
import com.google.cloud.functions.HttpRequest
import java.util.logging.Logger

class KotlinHelloBackground : BackgroundFunction<HttpRequest> {
    override fun accept(request: HttpRequest, context: Context) {
        var name = "world"
        if (request.getFirstQueryParameter("name").isPresent) {
            name = request.getFirstQueryParameter("name").get()
        }
        LOGGER.info(String.format("Hello %s!", name))
    }

    companion object {
        private val LOGGER = Logger.getLogger(KotlinHelloBackground::class.java.name)
    }
}
// [END functions_helloworld_background]