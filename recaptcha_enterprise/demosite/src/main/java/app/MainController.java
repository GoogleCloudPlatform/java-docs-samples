/*
 * Copyright 2023 Google LLC
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

package app;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;
import recaptcha.CreateAssessment;

@RestController
@RequestMapping
public class MainController {

  private static final LinkedHashMap<String, String> CONTEXT = new LinkedHashMap<>();

  static {
    CONTEXT.put("project_id", System.getenv("GOOGLE_CLOUD_PROJECT"));
    CONTEXT.put("site_key", System.getenv("SITE_KEY"));
  }

  @GetMapping(value = "/")
  public static ModelAndView home() {
    return new ModelAndView("home", CONTEXT);
  }

  @GetMapping(value = "/store")
  public static ModelAndView store() {
    return new ModelAndView("store", CONTEXT);
  }

  @GetMapping(value = "/login")
  public static ModelAndView login() {
    return new ModelAndView("login", CONTEXT);
  }

  @GetMapping(value = "/comment")
  public static ModelAndView comment() {
    return new ModelAndView("comment", CONTEXT);
  }

  @GetMapping(value = "/signup")
  public static ModelAndView signup() {
    return new ModelAndView("signup", CONTEXT);
  }

  @GetMapping(value = "/game")
  public static ModelAndView game() {
    return new ModelAndView("game", CONTEXT);
  }

  @PostMapping(value = "/create_assessment", produces = "application/json")
  public static @ResponseBody
  ResponseEntity<HashMap<String, HashMap<String, String>>> createAssessment(
      @RequestBody Map<String, HashMap<String, String>> credentials) {
    String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    HashMap<String, HashMap<String, String>> result;

    try {
      // <!-- ATTENTION: reCAPTCHA Example (Server Part 1/2) Starts -->
      result = CreateAssessment.createAssessment(projectId,
          CONTEXT.get("site_key"),
          credentials.get("recaptcha_cred").get("token"),
          credentials.get("recaptcha_cred").get("action"));
      // <!-- ATTENTION: reCAPTCHA Example (Server Part 1/2) Ends -->
    } catch (Exception e) {
      result = new HashMap<>() {{
        put("data", new HashMap<>() {{
          put("error_msg", e.toString());
        }});
      }};
      return new ResponseEntity<>(result, httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return new ResponseEntity<>(result, httpHeaders, HttpStatus.OK);
  }
}
