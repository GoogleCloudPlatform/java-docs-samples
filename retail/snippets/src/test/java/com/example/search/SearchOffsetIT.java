/*
 * Copyright 2026 Google LLC
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

package com.example.search;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.google.api.gax.rpc.InvalidArgumentException;
import com.google.cloud.retail.v2.Product;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class SearchOffsetIT {
  private static PrintStream origPrintStream;
  private static ByteArrayOutputStream bout;

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String VISITOR_ID = "test_visitor";
  private static final int NUM_PRODUCTS_TO_TEST = 2;
  private static final String PRODUCT_TITLE = "Hot Java Testing";
  private static List<Product> productsToTest;

  private static void requireEnvVar(String varName) {
    assertNotNull(
        "Environment variable " + varName + " is required to perform these tests.",
        System.getenv(varName));
  }

  @BeforeAll
  public static void setUp() throws InterruptedException, IOException {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    // Create products to be searched
    productsToTest = Utils.createProductsToTest(NUM_PRODUCTS_TO_TEST, PROJECT_ID, PRODUCT_TITLE);
    Utils.waitForProductsToBeReadyToTest(PROJECT_ID, productsToTest, PRODUCT_TITLE);

    origPrintStream = System.out;
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @AfterAll
  public static void tearDown() throws IOException {
    // Clean up products created for testing
    Utils.deleteProducts(productsToTest);

    System.setOut(origPrintStream);
  }

  @Test
  public void testSearchWithOffset() throws Exception {
    int offset = 1;
    SearchOffset.searchWithOffset(PROJECT_ID, VISITOR_ID, PRODUCT_TITLE, offset);
    String output = bout.toString();
    assertThat(output)
        .contains("Found " + (NUM_PRODUCTS_TO_TEST - offset) + " results in current page");
    assertThat(output).contains("Product Name:");
  }

  @Test
  public void testSearchWithOffset_offsetEqualsZero() throws Exception {
    int offset = 0;
    SearchOffset.searchWithOffset(PROJECT_ID, VISITOR_ID, PRODUCT_TITLE, offset);
    String output = bout.toString();
    assertThat(output).contains("Found " + NUM_PRODUCTS_TO_TEST + " results in current page");
    assertThat(output).contains("Product Name:");
  }

  @Test
  public void testSearchWithOffset_offsetTooLarge_foundZeroResults() throws Exception {
    int offset = 5;
    SearchOffset.searchWithOffset(PROJECT_ID, VISITOR_ID, PRODUCT_TITLE, offset);
    String output = bout.toString();
    assertThat(output).contains("Found 0 results in current page");
  }

  @Test
  public void testSearchWithOffset_negativeOffset_throwsInvalidArgumentException()
      throws Exception {
    int offset = -1;
    assertThrows(
        InvalidArgumentException.class,
        () -> SearchOffset.searchWithOffset(PROJECT_ID, VISITOR_ID, PRODUCT_TITLE, offset));
  }
}
