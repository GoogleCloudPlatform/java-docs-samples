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

import com.google.recaptchaenterprise.v1.WebKeySettings.IntegrationType;
import io.github.bonigarcia.wdm.WebDriverManager;
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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
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
public class ScoreKeyIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String DOMAIN_NAME = "localhost";
  private static String RECAPTCHA_SITE_KEY = "recaptcha-site-key";
  private static String HOME_BUTTON_XPATH = "//*[@id=\"example\"]/button";
  private static String BUTTON_XPATH = "/html/body/recaptcha-demo/button";
  private static String RESULT_XPATH = "//*[@id=\"result\"]/section/div/code/pre";
  private static WebDriver browser;
  @LocalServerPort
  private static int randomServerPort;

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
    WebDriverManager.chromedriver().setup();
    browser = new ChromeDriver();
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

  public static String makeRequest(String url, String siteKey) throws MalformedURLException {
    // Construct the URL to call for validating the assessment.
    return
        UriComponentsBuilder.fromUriString(url)
            // .queryParam("project_id", PROJECT_ID)
            // .queryParam("site_key", siteKey)
            .build()
            .encode()
            .toUri()
            .toURL()
            .toString();
  }

  public String browserTest(String pageUrl, String buttonXPath, String resultXPath,
      boolean testUrlRedirect)
      throws JSONException, InterruptedException {
    browser.get(pageUrl);

    // Wait until the page is loaded.
    JavascriptExecutor js = (JavascriptExecutor) browser;
    new WebDriverWait(browser, Duration.ofSeconds(10))
        .until(webDriver -> js.executeScript("return document.readyState").equals("complete"));

    if (!buttonXPath.isEmpty()) {
      browser.findElement(By.xpath(buttonXPath)).click();
      TimeUnit.SECONDS.sleep(1);
    }

    if (!testUrlRedirect) {
      // Retrieve the reCAPTCHA token response.
      return browser.findElement(By.xpath(resultXPath)).getText();
    }
    return browser.getCurrentUrl();
  }

  @Test
  public void testHomePage() throws MalformedURLException, InterruptedException {
    // Verify assessment.
    String testUrl = "http://localhost:" + randomServerPort + "/";
    String pageUrl = makeRequest(testUrl, RECAPTCHA_SITE_KEY);
    String resultXPath = "//*[@id=\"result\"]/section/div/code/pre";
    String result = browserTest(pageUrl, "", RESULT_XPATH, false);
    assertThat(result).contains("\"expectedAction\": \"home\"");
    assertThat(result).containsMatch(Pattern.compile("\"score\": \"(\\d*[.])?\\d+\""));

    // Verify redirection to signup.
    String redirectUrl = browserTest(pageUrl, HOME_BUTTON_XPATH, resultXPath, true);
    Assert.assertEquals(redirectUrl, testUrl.concat("signup"));
  }

  @Test
  public void testSignupPage() throws MalformedURLException, InterruptedException {
    // Verify assessment.
    String testUrl = "http://localhost:" + randomServerPort + "/";
    String pageUrl = makeRequest(testUrl.concat("signup"), RECAPTCHA_SITE_KEY);

    String result = browserTest(pageUrl, BUTTON_XPATH, RESULT_XPATH, false);
    assertThat(result).contains("\"expectedAction\": \"sign_up\"");
    assertThat(result).containsMatch(Pattern.compile("\"score\": \"(\\d*[.])?\\d+\""));

    // Verify redirection to login.
    String redirectUrl = browserTest(pageUrl, BUTTON_XPATH, RESULT_XPATH, true);
    Assert.assertEquals(redirectUrl, testUrl.concat("login"));
  }

  @Test
  public void testLoginPage() throws IOException, InterruptedException {
    // Verify assessment.
    String testUrl = "http://localhost:" + randomServerPort + "/";
    String pageUrl = makeRequest(testUrl.concat("login"), RECAPTCHA_SITE_KEY);
    String result = browserTest(pageUrl, BUTTON_XPATH, RESULT_XPATH, false);
    assertThat(result).contains("\"expectedAction\": \"log_in\"");
    assertThat(result).containsMatch(Pattern.compile("\"score\": \"(\\d*[.])?\\d+\""));

    // Verify redirection to store.
    String redirectUrl = browserTest(pageUrl, BUTTON_XPATH, RESULT_XPATH, true);
    Assert.assertEquals(redirectUrl, testUrl.concat("store"));
  }

  @Test
  public void testStorePage() throws MalformedURLException, InterruptedException {
    // Verify assessment.
    String testUrl = "http://localhost:" + randomServerPort + "/";
    String pageUrl = makeRequest(testUrl.concat("store"), RECAPTCHA_SITE_KEY);
    String result = browserTest(pageUrl, BUTTON_XPATH, RESULT_XPATH, false);
    assertThat(result).contains("\"expectedAction\": \"check_out\"");
    assertThat(result).containsMatch(Pattern.compile("\"score\": \"(\\d*[.])?\\d+\""));

    // Verify redirection to comment.
    String redirectUrl = browserTest(pageUrl, BUTTON_XPATH, RESULT_XPATH, true);
    Assert.assertEquals(redirectUrl, testUrl.concat("comment"));
  }

  @Test
  public void testCommentPage() throws MalformedURLException, InterruptedException {
    // Verify assessment.
    String testUrl = "http://localhost:" + randomServerPort + "/";
    String pageUrl = makeRequest(testUrl.concat("comment"), RECAPTCHA_SITE_KEY);
    String result = browserTest(pageUrl, BUTTON_XPATH, RESULT_XPATH, false);
    assertThat(result).contains("\"expectedAction\": \"send_comment\"");
    assertThat(result).containsMatch(Pattern.compile("\"score\": \"(\\d*[.])?\\d+\""));

    // Verify redirection to game.
    String redirectUrl = browserTest(pageUrl, BUTTON_XPATH, RESULT_XPATH, true);
    Assert.assertEquals(redirectUrl, testUrl.concat("game"));
  }

}
