package com.example.endpoints;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.Collections;
import java.util.Map;

@SpringBootApplication
public class EndpointsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EndpointsApplication.class, args);
    }

    @Bean
    public OpenAPI customOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Cloud Endpoints + Cloud Run")
                        .description("Sample API on Cloud Endpoints with a Cloud Run backend")
                        .version("1.0.0"))
                .servers(null)
                .extensions(Map.of(
                        "host", "<YOUR_HOST_HERE>-ue.a.run.app",
                        "schemes", Collections.singletonList("https"),
                        "produces", Collections.singletonList("application/json"),
                        "x-google-backend", Map.of(
                                "address", "https://<YOUR_APP_URI_HERE>-ue.a.run.app/",
                                "protocol", "h2"
                        )));
    }
}
