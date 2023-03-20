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

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.common.collect.ImmutableMap;
import com.google.recaptchaenterprise.v1.WebKeySettings.IntegrationType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.MalformedURLException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.regex.Pattern;
import org.json.JSONException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.SearchContext;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.web.util.UriComponentsBuilder;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@Ignore
public class ScoreKeyIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");

  private static final String CHROME_DRIVER_PATH = System.getenv("CHROME_DRIVER_PATH");
  private static final String DOMAIN_NAME = "localhost";
  private static String RECAPTCHA_SITE_KEY;

  // CSS selector of reCAPTCHA button in /home page. Used to execute Javascript actions.
  private static final String HOME_BUTTON_SELECTOR = "#example > button";

  // CSS selector of reCAPTCHA button in all pages, except /home. Used to execute Javascript actions.
  private static final String BUTTON_SELECTOR = "button";

  // CSS selector of reCAPTCHA result in page. Used to execute Javascript actions.
  private static final String RESULT_SELECTOR = "#result pre";

  private static final String SHADOW_HOST_SELECTOR = "recaptcha-demo";
  private static WebDriver browser;
  @LocalServerPort
  private int randomServerPort;

  private ByteArrayOutputStream stdOut;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName))
        .isNotEmpty();
  }

  @BeforeClass
  public static void setUp() throws IOException, InterruptedException {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    // Create reCAPTCHA Score key.
    RECAPTCHA_SITE_KEY = app.Util.createSiteKey(PROJECT_ID, DOMAIN_NAME, IntegrationType.SCORE);
    TimeUnit.SECONDS.sleep(5);

    // Set Selenium Driver to Chrome.
    ChromeOptions chromeOptions = new ChromeOptions().setBinary(CHROME_DRIVER_PATH);
    browser = new ChromeDriver(chromeOptions);
    TimeUnit.SECONDS.sleep(5);
  }

  @AfterClass
  public static void cleanUp()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    app.Util.deleteSiteKey(PROJECT_ID, RECAPTCHA_SITE_KEY);
    assertThat(stdOut.toString()).contains("reCAPTCHA Site key successfully deleted !");

    browser.quit();

    stdOut.close();
    System.setOut(null);
  }

  @Before
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @After
  public void afterEach() {
    stdOut = null;
    System.setOut(null);
  }

  // Construct the page URL with necessary context parameters.
  public static String makeRequest(String url, String siteKey) throws MalformedURLException {
    return
        UriComponentsBuilder.fromUriString(url)
            .queryParam("project_id", PROJECT_ID)
            .queryParam("site_key", siteKey)
            .build()
            .encode()
            .toUri()
            .toURL()
            .toString();
  }

  // Retrieve page and click the button specified by the element to obtain
  // response and redirect URL.
  public ImmutableMap<String, String> browserTest(String pageUrl, String buttonXPath,
      String resultXPath,
      boolean scoreOnPageLoad)
      throws JSONException, InterruptedException {
    browser.get(pageUrl);

    // Wait until the page is loaded.
    JavascriptExecutor js = (JavascriptExecutor) browser;
    new WebDriverWait(browser, Duration.ofSeconds(10))
        .until(webDriver -> js.executeScript("return document.readyState").equals("complete"));

    // Get the shadow root and its content.
    WebElement shadowHost = browser.findElement(By.cssSelector(SHADOW_HOST_SELECTOR));
    SearchContext shadowRoot = shadowHost.getShadowRoot();

    // If score is not available on page load, then click button to get score.
    if (!scoreOnPageLoad) {
      shadowHost.findElement(By.cssSelector(buttonXPath)).click();
      TimeUnit.SECONDS.sleep(1);
    }

    // Get the result. Sleep so that Selenium can get the updated text from the element.
    TimeUnit.SECONDS.sleep(1);
    WebElement response = shadowRoot.findElement(By.cssSelector(resultXPath));
    TimeUnit.SECONDS.sleep(1);
    String result = response.getText();

    // Click the button (again) to navigate to the next page. Based on the page, the button will
    // be located in either shadowRoot (home page) or shadowHost (all other pages).
    if (!scoreOnPageLoad) {
      shadowHost.findElement(By.cssSelector(buttonXPath)).click();
    } else {
      shadowRoot.findElement(By.cssSelector(buttonXPath)).click();
    }

    TimeUnit.SECONDS.sleep(1);
    String redirectedUrl = browser.getCurrentUrl();

    return ImmutableMap.of("result", result, "redirectedUrl", redirectedUrl);
  }

  @Test
  public void testHomePage() throws MalformedURLException, InterruptedException {
    // Home page URL.
    String testUrl = "http://localhost:" + randomServerPort + "/";
    String pageUrl = makeRequest(testUrl, RECAPTCHA_SITE_KEY);

    ImmutableMap<String, String> response =
        browserTest(pageUrl, HOME_BUTTON_SELECTOR, RESULT_SELECTOR, true);

    // Verify response contains expected action and a floating point score.
    String result = response.get("result");
    assertThat(result).contains("\"expectedAction\": \"home\"");
    assertThat(result).containsMatch(Pattern.compile("\"score\": \"(\\d*[.])?\\d+\""));

    // Verify redirection to signup.
    String redirectUrl = response.get("redirectedUrl");
    Assert.assertEquals(redirectUrl, testUrl.concat("signup"));
  }

  @Test
  public void testSignupPage() throws MalformedURLException, InterruptedException {
    // Signup page URL.
    String testUrl = "http://localhost:" + randomServerPort + "/";
    String pageUrl = makeRequest(testUrl.concat("signup"), RECAPTCHA_SITE_KEY);

    ImmutableMap<String, String> response =
        browserTest(pageUrl, BUTTON_SELECTOR, RESULT_SELECTOR, false);

    // Verify response contains expected action and a floating point score.
    String result = response.get("result");
    assertThat(result).contains("\"expectedAction\": \"sign_up\"");
    assertThat(result).containsMatch(Pattern.compile("\"score\": \"(\\d*[.])?\\d+\""));

    // Verify redirection to login.
    String redirectUrl = response.get("redirectedUrl");
    Assert.assertEquals(redirectUrl, testUrl.concat("login"));
  }

  @Test
  public void testLoginPage() throws IOException, InterruptedException {
    // Login page URL.
    String testUrl = "http://localhost:" + randomServerPort + "/";
    String pageUrl = makeRequest(testUrl.concat("login"), RECAPTCHA_SITE_KEY);

    ImmutableMap<String, String> response =
        browserTest(pageUrl, BUTTON_SELECTOR, RESULT_SELECTOR, false);

    // Verify response contains expected action and a floating point score.
    String result = response.get("result");
    assertThat(result).contains("\"expectedAction\": \"log_in\"");
    assertThat(result).containsMatch(Pattern.compile("\"score\": \"(\\d*[.])?\\d+\""));

    // Verify redirection to store.
    String redirectUrl = response.get("redirectedUrl");
    Assert.assertEquals(redirectUrl, testUrl.concat("store"));
  }

  @Test
  public void testStorePage() throws MalformedURLException, InterruptedException {
    // Store page URL.
    String testUrl = "http://localhost:" + randomServerPort + "/";
    String pageUrl = makeRequest(testUrl.concat("store"), RECAPTCHA_SITE_KEY);

    ImmutableMap<String, String> response =
        browserTest(pageUrl, BUTTON_SELECTOR, RESULT_SELECTOR, false);

    // Verify response contains expected action and a floating point score.
    String result = response.get("result");
    assertThat(result).contains("\"expectedAction\": \"check_out\"");
    assertThat(result).containsMatch(Pattern.compile("\"score\": \"(\\d*[.])?\\d+\""));

    // Verify redirection to comment.
    String redirectUrl = response.get("redirectedUrl");
    Assert.assertEquals(redirectUrl, testUrl.concat("comment"));
  }

  @Test
  public void testCommentPage() throws MalformedURLException, InterruptedException {
    // Comment page URL.
    String testUrl = "http://localhost:" + randomServerPort + "/";
    String pageUrl = makeRequest(testUrl.concat("comment"), RECAPTCHA_SITE_KEY);

    ImmutableMap<String, String> response =
        browserTest(pageUrl, BUTTON_SELECTOR, RESULT_SELECTOR, false);

    // Verify response contains expected action and a floating point score.
    String result = response.get("result");
    assertThat(result).contains("\"expectedAction\": \"send_comment\"");
    assertThat(result).containsMatch(Pattern.compile("\"score\": \"(\\d*[.])?\\d+\""));

    // Verify redirection to game.
    String redirectUrl = response.get("redirectedUrl");
    Assert.assertEquals(redirectUrl, testUrl.concat("game"));
  }
}
