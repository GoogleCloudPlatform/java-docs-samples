package com.example.endpoints.controllers;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiIssuer;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Api(
    name = "repeat",
    version = "v1",
    namespace = @ApiNamespace(
        ownerDomain = "repeat.example.com",
        ownerName = "repeat.example.com"
    ),
    issuers = { @ApiIssuer(
        name = "firebase",
        issuer = "https://securetoken.google.com/YOUR-PROJECT-ID",
        jwksUri = "https://www.googleapis.com/service_accounts/v1/metadata/x509/securetoken@system.gserviceaccount.com"
    )}
)
@Controller
public class RepeatController {

    @GetMapping("/repeat")
    @ApiMethod(name = "repeat", path = "repeat", httpMethod = ApiMethod.HttpMethod.GET)
    public ResponseEntity<String> repeat(@RequestParam("text") @Named("text") String text,
                                         @RequestParam("times") @Named("times") Integer times) {
        StringBuilder response = new StringBuilder();
        for(int i = 0; i < times - 1; i++) {
            response.append(text).append(", ");
        }
        response.append(text).append("!");

        return new ResponseEntity<String>(response.toString(), HttpStatus.OK);
    }
}
