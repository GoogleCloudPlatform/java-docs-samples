package com.example.endpoints.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RequestMapping("/api/v1")
@RestController
public class RepeatController {

    @GetMapping("/repeat")
    public ResponseEntity<String> repeat(@RequestParam("text") String text,
                                         @RequestParam("times") Integer times) {
        StringBuilder response = new StringBuilder();
        for(int i = 0; i < times - 1; i++) {
            response.append(text).append(", ");
        }
        response.append(text).append("!");

        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }
}
