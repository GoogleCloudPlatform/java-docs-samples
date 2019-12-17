/*
 * Copyright 2019 Google LLC
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

package com.example.getstarted.actions;

import static org.junit.Assert.assertEquals;

import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.FirestoreOptions;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import java.util.concurrent.ExecutionException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.service.DriverService;

@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class UserJourneyTestIT {
  private static DriverService service;
  private WebDriver driver;

  @BeforeClass
  public static void setupClass() throws Exception {
    service = ChromeDriverService.createDefaultService();
    service.start();
  }

  @AfterClass
  public static void tearDownClass() throws ExecutionException, InterruptedException {
    // Clear the firestore sessions data.
    Firestore firestore = FirestoreOptions.getDefaultInstance().getService();
    for (QueryDocumentSnapshot docSnapshot :
        firestore.collection("books").get().get().getDocuments()) {
      docSnapshot.getReference().delete().get();
    }

    service.stop();
  }

  @Before
  public void setup() {
    driver = new ChromeDriver();
  }

  @After
  public void tearDown() {
    driver.quit();
  }

  @Test
  public void userJourney() {
    // Do selenium tests on the deployed version, if applicable
    String endpoint = "http://localhost:8080";
    System.out.println("Testing endpoint: " + endpoint);
    driver.get(endpoint);

    WebElement body = driver.findElement(By.cssSelector("body"));

    String responseText = body.getText();
    // Reload the page to ensure the session data returns the same text except with 1 more view.
    driver.get(endpoint);
    body = driver.findElement(By.cssSelector("body"));
    String response2Text = body.getText();
    assertEquals(responseText, response2Text);
  }
}
