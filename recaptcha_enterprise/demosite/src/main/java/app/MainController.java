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

import com.google.recaptchaenterprise.v1.Assessment;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
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

  // Sample threshold score for classification of bad / not bad action. The threshold score
  // can be used to trigger secondary actions like MFA.
  private static final double SAMPLE_THRESHOLD_SCORE;
  private static final LinkedHashMap<String, String> CONTEXT = new LinkedHashMap<>();
  private static final Properties PROPERTIES = new Properties();

  static {
    SAMPLE_THRESHOLD_SCORE = 0.50;

    CONTEXT.put("project_id", System.getenv("GOOGLE_CLOUD_PROJECT"));
    CONTEXT.put("site_key", System.getenv("SITE_KEY"));

    // Parse property file and read available reCAPTCHA actions. All reCAPTCHA actions registered
    // in the client should be mapped in the config file. This will be used to verify if the token
    // obtained during assessment corresponds to the claimed action.
    try (InputStream input = MainController.class.getClassLoader()
        .getResourceAsStream("config.properties")) {
      PROPERTIES.load(input);
    } catch (Exception e) {
      System.out.println("Exception while loading property file...");
    }
  }

  /**
   * Return homepage template.
   */
  @GetMapping(value = "/")
  public static ModelAndView home() {
    return new ModelAndView("home", CONTEXT);
  }

  /**
   * On homepage load, execute reCAPTCHA Enterprise assessment and take action according to the
   * score.
   */
  @PostMapping(value = "/on_homepage_load", produces = "application/json")
  public static @ResponseBody ResponseEntity<HashMap<String, HashMap<String, String>>> onHomepageLoad(
      @RequestBody Map<String, HashMap<String, String>> jsonData) {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    String recaptchaAction = PROPERTIES.getProperty("recaptcha_action.home");
    HashMap<String, HashMap<String, String>> data;
    Assessment assessmentResponse;
    String verdict;

    try {
      // <!-- ATTENTION: reCAPTCHA Example (Server Part 1/2) Starts -->
      assessmentResponse = CreateAssessment.createAssessment(
          CONTEXT.get("project_id"),
          CONTEXT.get("site_key"),
          jsonData.get("recaptcha_cred").get("token"));

      // Check if the token is valid, score is above threshold score and the action equals expected.
      if (assessmentResponse.getTokenProperties().getValid() &&
          assessmentResponse.getRiskAnalysis().getScore() > SAMPLE_THRESHOLD_SCORE &&
          assessmentResponse.getTokenProperties().getAction().equals(recaptchaAction)) {
        // Load the home page.
        // Business logic.
        // Classify the action as not bad.
        verdict = "Not Bad";
      } else {
        // If any of the above condition fails, trigger email/ phone verification flow.
        // Classify the action as bad.
        verdict = "Bad";
      }
      // <!-- ATTENTION: reCAPTCHA Example (Server Part 1/2) Ends -->

      // Return the risk score.
      HashMap<String, String> result = new HashMap<>() {{
        put("score", String.valueOf(assessmentResponse.getRiskAnalysis().getScore()));
        put("verdict", verdict);
      }};
      data = new HashMap<>() {{
        put("data", result);
      }};
      return new ResponseEntity<>(data, httpHeaders, HttpStatus.OK);
    } catch (Exception e) {
      data = new HashMap<>() {{
        put("data", new HashMap<>() {{
          put("error_msg", e.toString());
        }});
      }};
      return new ResponseEntity<>(data, httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Return signup template.
   */
  @GetMapping(value = "/signup")
  public static ModelAndView signup() {
    return new ModelAndView("signup", CONTEXT);
  }

  /**
   * On signup button click, execute reCAPTCHA Enterprise assessment and take action according to
   * the score.
   */
  @PostMapping(value = "/on_signup", produces = "application/json")
  public static @ResponseBody ResponseEntity<HashMap<String, HashMap<String, String>>> onSignup(
      @RequestBody Map<String, HashMap<String, String>> jsonData) {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    String recaptchaAction = PROPERTIES.getProperty("recaptcha_action.signup");
    HashMap<String, HashMap<String, String>> data;
    Assessment assessmentResponse;
    String verdict;

    try {
      // <!-- ATTENTION: reCAPTCHA Example (Server Part 1/2) Starts -->
      assessmentResponse = CreateAssessment.createAssessment(
          CONTEXT.get("project_id"),
          CONTEXT.get("site_key"),
          jsonData.get("recaptcha_cred").get("token"));

      // Check if the token is valid, score is above threshold score and the action equals expected.
      if (assessmentResponse.getTokenProperties().getValid() &&
          assessmentResponse.getRiskAnalysis().getScore() > SAMPLE_THRESHOLD_SCORE &&
          assessmentResponse.getTokenProperties().getAction().equals(recaptchaAction)) {
        // Write new username and password to users database.
        // String username = jsonData.get("recaptcha_cred").get("username");
        // String password = jsonData.get("recaptcha_cred").get("password");
        // Business logic.
        // Classify the action as not bad.
        verdict = "Not Bad";
      } else {
        // If any of the above condition fails, trigger email/ phone verification flow.
        // Classify the action as bad.
        verdict = "Bad";
      }
      // <!-- ATTENTION: reCAPTCHA Example (Server Part 1/2) Ends -->

      // Return the risk score.
      HashMap<String, String> result = new HashMap<>() {{
        put("score", String.valueOf(assessmentResponse.getRiskAnalysis().getScore()));
        put("verdict", verdict);
      }};
      data = new HashMap<>() {{
        put("data", result);
      }};
      return new ResponseEntity<>(data, httpHeaders, HttpStatus.OK);
    } catch (Exception e) {
      data = new HashMap<>() {{
        put("data", new HashMap<>() {{
          put("error_msg", e.toString());
        }});
      }};
      return new ResponseEntity<>(data, httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Return login template.
   */
  @GetMapping(value = "/login")
  public static ModelAndView login() {
    return new ModelAndView("login", CONTEXT);
  }

  /**
   * On login button click, execute reCAPTCHA Enterprise assessment and take action according to the
   * score.
   */
  @PostMapping(value = "/on_login", produces = "application/json")
  public static @ResponseBody ResponseEntity<HashMap<String, HashMap<String, String>>> onLogin(
      @RequestBody Map<String, HashMap<String, String>> jsonData) {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    String recaptchaAction = PROPERTIES.getProperty("recaptcha_action.login");
    HashMap<String, HashMap<String, String>> data;
    Assessment assessmentResponse;
    String verdict;

    try {
      // <!-- ATTENTION: reCAPTCHA Example (Server Part 1/2) Starts -->
      assessmentResponse = CreateAssessment.createAssessment(
          CONTEXT.get("project_id"),
          CONTEXT.get("site_key"),
          jsonData.get("recaptcha_cred").get("token"));

      // Check if the token is valid, score is above threshold score and the action equals expected.
      if (assessmentResponse.getTokenProperties().getValid() &&
          assessmentResponse.getRiskAnalysis().getScore() > SAMPLE_THRESHOLD_SCORE &&
          assessmentResponse.getTokenProperties().getAction().equals(recaptchaAction)) {
        // Check if the login credentials exist and match.
        // String username = jsonData.get("recaptcha_cred").get("username");
        // String password = jsonData.get("recaptcha_cred").get("password");
        // Business logic.
        // Classify the action as not bad.
        verdict = "Not Bad";
      } else {
        // If any of the above condition fails, trigger email/ phone verification flow.
        // Classify the action as bad.
        verdict = "Bad";
      }
      // <!-- ATTENTION: reCAPTCHA Example (Server Part 1/2) Ends -->

      // Return the risk score.
      HashMap<String, String> result = new HashMap<>() {{
        put("score", String.valueOf(assessmentResponse.getRiskAnalysis().getScore()));
        put("verdict", verdict);
      }};
      data = new HashMap<>() {{
        put("data", result);
      }};
      return new ResponseEntity<>(data, httpHeaders, HttpStatus.OK);
    } catch (Exception e) {
      data = new HashMap<>() {{
        put("data", new HashMap<>() {{
          put("error_msg", e.toString());
        }});
      }};
      return new ResponseEntity<>(data, httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Return store template.
   */
  @GetMapping(value = "/store")
  public static ModelAndView store() {
    return new ModelAndView("store", CONTEXT);
  }

  /**
   * On checkout button click in store page, execute reCAPTCHA Enterprise assessment and take action
   * according to the score.
   */
  @PostMapping(value = "/on_store_checkout", produces = "application/json")
  public static @ResponseBody ResponseEntity<HashMap<String, HashMap<String, String>>> onStoreCheckout(
      @RequestBody Map<String, HashMap<String, String>> jsonData) {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    String recaptchaAction = PROPERTIES.getProperty("recaptcha_action.store");
    HashMap<String, HashMap<String, String>> data;
    Assessment assessmentResponse;
    String verdict;

    try {
      // <!-- ATTENTION: reCAPTCHA Example (Server Part 1/2) Starts -->
      assessmentResponse = CreateAssessment.createAssessment(
          CONTEXT.get("project_id"),
          CONTEXT.get("site_key"),
          jsonData.get("recaptcha_cred").get("token"));

      // Check if the token is valid, score is above threshold score and the action equals expected.
      if (assessmentResponse.getTokenProperties().getValid() &&
          assessmentResponse.getRiskAnalysis().getScore() > SAMPLE_THRESHOLD_SCORE &&
          assessmentResponse.getTokenProperties().getAction().equals(recaptchaAction)) {
        // Check if the cart contains items and proceed to checkout and payment.
        // items = jsonData.get("recaptcha_cred").get("items");
        // Business logic.
        // Classify the action as not bad.
        verdict = "Not Bad";
      } else {
        // If any of the above condition fails, trigger email/ phone verification flow.
        // Classify the action as bad.
        verdict = "Bad";
      }
      // <!-- ATTENTION: reCAPTCHA Example (Server Part 1/2) Ends -->

      // Return the risk score.
      HashMap<String, String> result = new HashMap<>() {{
        put("score", String.valueOf(assessmentResponse.getRiskAnalysis().getScore()));
        put("verdict", verdict);
      }};
      data = new HashMap<>() {{
        put("data", result);
      }};
      return new ResponseEntity<>(data, httpHeaders, HttpStatus.OK);
    } catch (Exception e) {
      data = new HashMap<>() {{
        put("data", new HashMap<>() {{
          put("error_msg", e.toString());
        }});
      }};
      return new ResponseEntity<>(data, httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Return comment template.
   */
  @GetMapping(value = "/comment")
  public static ModelAndView comment() {
    return new ModelAndView("comment", CONTEXT);
  }

  /**
   * On comment submit, execute reCAPTCHA Enterprise assessment and take action according to the
   * score.
   */
  @PostMapping(value = "/on_comment_submit", produces = "application/json")
  public static @ResponseBody ResponseEntity<HashMap<String, HashMap<String, String>>> onCommentSubmit(
      @RequestBody Map<String, HashMap<String, String>> jsonData) {
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    String recaptchaAction = PROPERTIES.getProperty("recaptcha_action.comment");
    HashMap<String, HashMap<String, String>> data;
    Assessment assessmentResponse;
    String verdict;

    try {
      // <!-- ATTENTION: reCAPTCHA Example (Server Part 1/2) Starts -->
      assessmentResponse = CreateAssessment.createAssessment(
          CONTEXT.get("project_id"),
          CONTEXT.get("site_key"),
          jsonData.get("recaptcha_cred").get("token"));

      // Check if the token is valid, score is above threshold score and the action equals expected.
      if (assessmentResponse.getTokenProperties().getValid() &&
          assessmentResponse.getRiskAnalysis().getScore() > SAMPLE_THRESHOLD_SCORE &&
          assessmentResponse.getTokenProperties().getAction().equals(recaptchaAction)) {
        // Check if comment has safe language and proceed to store in database.
        // String comment = jsonData.get("recaptcha_cred").get("comment");
        // Business logic.
        // Classify the action as not bad.
        verdict = "Not Bad";
      } else {
        // If any of the above condition fails, trigger email/ phone verification flow.
        // Classify the action as bad.
        verdict = "Bad";
      }
      // <!-- ATTENTION: reCAPTCHA Example (Server Part 1/2) Ends -->

      // Return the risk score.
      HashMap<String, String> result = new HashMap<>() {{
        put("score", String.valueOf(assessmentResponse.getRiskAnalysis().getScore()));
        put("verdict", verdict);
      }};
      data = new HashMap<>() {{
        put("data", result);
      }};
      return new ResponseEntity<>(data, httpHeaders, HttpStatus.OK);
    } catch (Exception e) {
      data = new HashMap<>() {{
        put("data", new HashMap<>() {{
          put("error_msg", e.toString());
        }});
      }};
      return new ResponseEntity<>(data, httpHeaders, HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }

  /**
   * Return game template.
   */
  @GetMapping(value = "/game")
  public static ModelAndView game() {
    return new ModelAndView("game", CONTEXT);
  }

}
