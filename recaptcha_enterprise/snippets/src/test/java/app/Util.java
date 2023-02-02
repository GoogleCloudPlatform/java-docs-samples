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

import com.google.protobuf.ByteString;
import io.github.bonigarcia.wdm.WebDriverManager;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.json.JSONException;
import org.json.JSONObject;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.web.util.UriComponentsBuilder;

public class Util {

  private static final WebDriver browser;

  static {
    // Set Selenium Driver to Chrome.
    WebDriverManager.chromedriver().setup();
    browser = new ChromeDriver();
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

    browser.get(url.toURL().toString());

    // Wait until the page is loaded.
    JavascriptExecutor js = (JavascriptExecutor) browser;
    new WebDriverWait(browser, Duration.ofSeconds(10))
        .until(webDriver -> js.executeScript("return document.readyState").equals("complete"));

    // Pass the values to be entered.
    browser.findElement(By.id("username")).sendKeys("username");
    browser.findElement(By.id("password")).sendKeys("password");

    // On clicking the button, the request params will be sent to reCAPTCHA.
    browser.findElement(By.id("recaptchabutton")).click();

    TimeUnit.SECONDS.sleep(1);

    // Retrieve the reCAPTCHA token response.
    WebElement element = browser.findElement(By.cssSelector("#assessment"));
    String token = element.getAttribute("data-token");
    String action = element.getAttribute("data-action");

    browser.quit();
    return new JSONObject().put("token", token).put("action", action);
  }

  public static String substring(String line) {
    return line.substring((line.lastIndexOf(":") + 1)).trim();
  }

  public static String getAssessmentName(String stdout) {
    String assessmentName = "";
    for (String line : stdout.split("\n")) {
      if (line.contains("Assessment name: ")) {
        assessmentName = Util.substring(line);
        return assessmentName;
      }
    }
    return "";
  }

  public static ByteString createHashedAccountId()
      throws NoSuchAlgorithmException, InvalidKeyException {
    String userIdentifier = "Alice Bob";
    // Change this to a secret not shared with Google.
    final String HMAC_KEY = "123456789";
    // Get instance of Mac object implementing HmacSHA256, and initialize it with the above
    // secret key.
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(HMAC_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    byte[] hashBytes = mac.doFinal(userIdentifier.getBytes(StandardCharsets.UTF_8));
    return ByteString.copyFrom(hashBytes);
  }

}
