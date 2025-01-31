package app;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

  @GetMapping("/")
  public String home(Model model) {
    return "index"; // Refers to templates/index.html
  }

  @GetMapping("/login")
  public String login(Model model) {
    return "login"; // Refers to templates/login.html
  }

  @GetMapping("/register")
  public String logout(Model model) {
    return "register"; // Refers to templates/register.html
  }
}
