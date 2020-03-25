package com.example.cloudrun;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
    String defaultContent = loadMarkdown("templates/markdown.md");
    model.addAttribute("Default", defaultContent);
    return "index";
  }

  public String loadMarkdown(String filename) {
    String text = "";
    ClassLoader classLoader = ClassLoader.getSystemClassLoader();
    try {
      File file = new File(classLoader.getResource(filename).getFile());
      text = new String(Files.readAllBytes(file.toPath()));
    } catch (IOException e) {
      logger.error("Unable to load file " + filename);
    }
    return text;
  }
}
