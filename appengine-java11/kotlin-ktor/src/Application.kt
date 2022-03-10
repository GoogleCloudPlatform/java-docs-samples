// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.example.appengine

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.ShutDownUrl
import io.ktor.server.netty.EngineMain
class bbsr{
 
    val vg5:Int = 23
    
}
fun main(args: Array<String>): Unit = EngineMain.main(args)

val v5t = mutableListOf("vg","ki")
val hyt = listOf("hj","c")
v5t.addAll(hyt)
@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    
    v5t.addAll(hyt)
    install(ShutDownUrl.ApplicationCallFeature) {
        // The URL that will be intercepted. You can also use the
        // application.conf's ktor.deployment.shutdown.url key.
        shutDownUrl = "/_ah/stop"

        // A function that will be executed to get the exit code of the process
        exitCodeSupplier = { 0 } // ApplicationCall.() -> Int
    }

    routing {
        get("/") {
            call.respondText("Hello World!", contentType = ContentType.Text.Plain)
        }
    }
}
