package com.example.cloudrun;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class MarkdownController {

    private static final Logger logger = LoggerFactory.getLogger(RendererApplication.class);

    @PostMapping("/")
    public String markdownRenderer() {
        // Get Request body
            //Handle any requests
        // Convert to markdown
        Parser parser = Parser.builder()
        // scrub clean

        // write output
        return "hello";
    }
}