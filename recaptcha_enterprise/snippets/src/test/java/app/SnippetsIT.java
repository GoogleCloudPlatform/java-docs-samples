/*
 * Copyright 2021 Google LLC
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

import account_defender.AccountDefenderAssessment;
import account_defender.AnnotateAccountDefenderAssessment;
import account_defender.ListRelatedAccountGroupMemberships;
import account_defender.ListRelatedAccountGroups;
import account_defender.SearchRelatedAccountGroupMemberships;
import com.google.protobuf.ByteString;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URI;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.json.JSONException;
import org.json.JSONObject;
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
import recaptcha.AnnotateAssessment;
import recaptcha.GetMetrics;
import recaptcha.mfa.CreateMfaAssessment;

@RunWith(SpringJUnit4ClassRunner.class)
@EnableAutoConfiguration
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
public class SnippetsIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String CHROME_DRIVER_PATH = System.getenv("CHROME_DRIVER_PATH");
  private static final String DOMAIN_NAME = "localhost";
  private static String RECAPTCHA_SITE_KEY_1 = "recaptcha-site-key1";
  private static String RECAPTCHA_SITE_KEY_2 = "recaptcha-site-key2";
  @LocalServerPort
  private int randomServerPort;
  private ByteArrayOutputStream stdOut;

  private static WebDriver BROWSER;

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

    // Create reCAPTCHA Site key and associate the given domain.
    RECAPTCHA_SITE_KEY_1 = recaptcha.CreateSiteKey.createSiteKey(PROJECT_ID, DOMAIN_NAME);
    RECAPTCHA_SITE_KEY_2 = recaptcha.CreateSiteKey.createSiteKey(PROJECT_ID, DOMAIN_NAME);
    TimeUnit.SECONDS.sleep(5);

    // Set Selenium Driver to Chrome.
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--headless");
    options.addArguments("--no-sandbox'");
    options.addArguments("--disable-gpu");
    BROWSER = WebDriverManager.chromedriver().capabilities(options).create();
    TimeUnit.SECONDS.sleep(5);
  }

  @AfterClass
  public static void cleanUp()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));

    recaptcha.DeleteSiteKey.deleteSiteKey(PROJECT_ID, RECAPTCHA_SITE_KEY_1);
    assertThat(stdOut.toString()).contains("reCAPTCHA Site key successfully deleted !");

    BROWSER.quit();

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

  public static JSONObject initiateBrowserTest(String testURL, String recaptchaKey)
      throws IOException, JSONException, InterruptedException {
    // Construct the URL to call for validating the assessment.
    URI url =
        UriComponentsBuilder.fromUriString(testURL)
            .queryParam("recaptchaSiteKey", recaptchaKey)
            .build()
            .encode()
            .toUri();

    BROWSER.get(url.toURL().toString());

    // Wait until the page is loaded.
    JavascriptExecutor js = (JavascriptExecutor) BROWSER;
    new WebDriverWait(BROWSER, Duration.ofSeconds(10))
        .until(webDriver -> js.executeScript("return document.readyState").equals("complete"));

    // Pass the values to be entered.
    BROWSER.findElement(By.id("username")).sendKeys("username");
    BROWSER.findElement(By.id("password")).sendKeys("password");

    // On clicking the button, the request params will be sent to reCAPTCHA.
    BROWSER.findElement(By.id("recaptchabutton")).click();

    TimeUnit.SECONDS.sleep(1);

    // Retrieve the reCAPTCHA token response.
    WebElement element = BROWSER.findElement(By.cssSelector("#assessment"));
    String token = element.getAttribute("data-token");
    String action = element.getAttribute("data-action");

    return new JSONObject().put("token", token).put("action", action);
  }

  @Test
  public void testCreateSiteKey() {
    // Test if the recaptcha site key has already been successfully created, as part of the setup.
    Assert.assertFalse(RECAPTCHA_SITE_KEY_1.isEmpty());
  }

  @Test
  public void testGetKey()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    recaptcha.GetSiteKey.getSiteKey(PROJECT_ID, RECAPTCHA_SITE_KEY_1);
    assertThat(stdOut.toString()).contains(RECAPTCHA_SITE_KEY_1);
  }

  @Test
  public void testListSiteKeys() throws IOException {
    recaptcha.ListSiteKeys.listSiteKeys(PROJECT_ID);
    assertThat(stdOut.toString()).contains(RECAPTCHA_SITE_KEY_1);
  }

  @Test
  public void testUpdateSiteKey()
      throws IOException, InterruptedException, ExecutionException, TimeoutException {
    recaptcha.UpdateSiteKey.updateSiteKey(PROJECT_ID, RECAPTCHA_SITE_KEY_1, DOMAIN_NAME);
    assertThat(stdOut.toString()).contains("reCAPTCHA Site key successfully updated !");
  }

  @Test
  public void testDeleteSiteKey()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    recaptcha.DeleteSiteKey.deleteSiteKey(PROJECT_ID, RECAPTCHA_SITE_KEY_2);
    assertThat(stdOut.toString()).contains("reCAPTCHA Site key successfully deleted !");
  }

  @Test
  public void testGetMetrics() throws IOException {
    GetMetrics.getMetrics(PROJECT_ID, RECAPTCHA_SITE_KEY_1);
    assertThat(stdOut.toString())
        .contains("Retrieved the bucket count for score based key: " + RECAPTCHA_SITE_KEY_1);
  }

  @Test
  public void testCreateAnnotateAssessment()
      throws JSONException, IOException, InterruptedException {
    String testURL = "http://localhost:" + randomServerPort + "/";
    JSONObject tokenActionPair = initiateBrowserTest(testURL, RECAPTCHA_SITE_KEY_1);

    recaptcha.CreateAssessment.createAssessment(
        PROJECT_ID,
        RECAPTCHA_SITE_KEY_1,
        tokenActionPair.getString("token"),
        tokenActionPair.getString("action"));

    assertThat(stdOut.toString()).contains("Assessment name: ");
    assertThat(stdOut.toString()).contains("The reCAPTCHA score is: ");
    String assessmentName = Util.getAssessmentName(stdOut.toString());
    assertThat(assessmentName).isNotEmpty();

    // Annotate the assessment.
    AnnotateAssessment.annotateAssessment(PROJECT_ID, assessmentName);
    assertThat(stdOut.toString()).contains("Annotated response sent successfully ! ");
  }

  @Test
  public void testCreateAnnotateAccountDefender()
      throws JSONException, IOException, NoSuchAlgorithmException, InvalidKeyException, InterruptedException {

    String testURL = "http://localhost:" + randomServerPort + "/";
    ByteString hashedAccountId = Util.createHashedAccountId();

    // Create the assessment.
    JSONObject tokenActionPair = initiateBrowserTest(testURL, RECAPTCHA_SITE_KEY_1);
    AccountDefenderAssessment.accountDefenderAssessment(
        PROJECT_ID,
        RECAPTCHA_SITE_KEY_1,
        tokenActionPair.getString("token"),
        tokenActionPair.getString("action"),
        hashedAccountId);
    String response = stdOut.toString();
    assertThat(response).contains("Assessment name: ");
    assertThat(response).contains("The reCAPTCHA score is: ");
    assertThat(response).contains("Account Defender Assessment Result: ");
    String assessmentName = Util.getAssessmentName(response);
    assertThat(assessmentName).isNotEmpty();

    // Annotate the assessment.
    AnnotateAccountDefenderAssessment.annotateAssessment(
        PROJECT_ID, assessmentName, hashedAccountId);
    assertThat(stdOut.toString()).contains("Annotated response sent successfully ! ");

    // List related account groups in the project.
    ListRelatedAccountGroups.listRelatedAccountGroups(PROJECT_ID);
    assertThat(stdOut.toString()).contains("Listing related account groups..");

    // List related account group memberships.
    ListRelatedAccountGroupMemberships.listRelatedAccountGroupMemberships(PROJECT_ID, "legitimate");
    assertThat(stdOut.toString()).contains("Finished listing related account group memberships.");

    // Search related group memberships for a hashed account id.
    SearchRelatedAccountGroupMemberships.searchRelatedAccountGroupMemberships(
        PROJECT_ID, hashedAccountId);
    assertThat(stdOut.toString())
        .contains(
            String.format(
                "Finished searching related account group memberships for %s", hashedAccountId));
  }

  @Test
  public void testPasswordLeakAssessment()
      throws JSONException, IOException, ExecutionException, InterruptedException {
    String testURL = "http://localhost:" + randomServerPort + "/";
    JSONObject tokenActionPair = initiateBrowserTest(testURL, RECAPTCHA_SITE_KEY_1);

    passwordleak.CreatePasswordLeakAssessment.checkPasswordLeak(
        PROJECT_ID,
        RECAPTCHA_SITE_KEY_1,
        tokenActionPair.getString("token"),
        tokenActionPair.getString("action"));

    assertThat(stdOut.toString()).contains("Assessment name: ");
    assertThat(stdOut.toString()).contains("The reCAPTCHA score is: ");
  }

  @Test
  public void testMultiFactorAuthenticationAssessment()
      throws IOException, JSONException, NoSuchAlgorithmException, InvalidKeyException, InterruptedException {
    ByteString hashedAccountId = Util.createHashedAccountId();
    String testURL = "http://localhost:" + randomServerPort + "/";
    JSONObject tokenActionPair = initiateBrowserTest(testURL, RECAPTCHA_SITE_KEY_1);

    CreateMfaAssessment.createMfaAssessment(PROJECT_ID, RECAPTCHA_SITE_KEY_1,
        tokenActionPair.getString("token"), tokenActionPair.getString("action"),
        hashedAccountId, "foo@bar.com", "+11111111111");
    assertThat(stdOut.toString()).contains("Result unspecified");
  }
}
