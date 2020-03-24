package com.example.cloudrun;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class EditorController {

    private static final Logger logger = LoggerFactory.getLogger(EditorController.class);

    @GetMapping("/")
    public String getIndex(Model model) {
        // Get default markdown
        // add to template
        return "index";
    }

      
  public void newServiceFromEnv() {
    String url = System.getenv("EDITOR_UPSTREAM_RENDER_URL");
    if (url == null) {
      logger.error("No configuration for upstream render service: add EDITOR_UPSTREAM_RENDER_URL environment variable");
      return null;
    }

    String auth = System.getenv("EDITOR_UPSTREAM_UNAUTHENTICATED");
    if (auth == null) {
      logger.warn("Editor: starting in unauthenticated upstream mode");
    }
    //create serivice
  }
}