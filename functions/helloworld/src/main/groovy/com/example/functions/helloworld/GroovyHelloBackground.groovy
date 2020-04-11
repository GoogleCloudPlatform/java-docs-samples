package com.example.functions.helloworld

// [START functions_helloworld_background]
import com.google.cloud.functions.BackgroundFunction
import com.google.cloud.functions.Context
import com.google.cloud.functions.HttpRequest

import java.util.logging.Logger;

class GroovyHelloBackground implements BackgroundFunction<HttpRequest> {
    private static final Logger LOGGER = Logger.getLogger(GroovyHelloBackground.class.name)

    @Override
    void accept(HttpRequest request, Context context) {
        // name's default value is "world"
        String name = request.getFirstQueryParameter("name").orElse("world");
        LOGGER.info(String.format("Hello %s!", name))
    }
}
// [END functions_helloworld_background]
