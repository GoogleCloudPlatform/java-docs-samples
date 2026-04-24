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

import com.google.cloud.retail.v2.BranchName;
import com.google.cloud.retail.v2.CreateProductRequest;
import com.google.cloud.retail.v2.DeleteProductRequest;
import com.google.cloud.retail.v2.PriceInfo;
import com.google.cloud.retail.v2.Product;
import com.google.cloud.retail.v2.Product.Availability;
import com.google.cloud.retail.v2.Product.Type;
import com.google.cloud.retail.v2.ProductServiceClient;
import com.google.cloud.retail.v2.SearchRequest;
import com.google.cloud.retail.v2.SearchServiceClient;
import com.google.cloud.retail.v2.SearchServiceClient.SearchPagedResponse;
import com.google.cloud.retail.v2.ServingConfigName;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Utils {

  public static List<Product> createProductsToTest(
      int amount, String projectId, String productTitle) throws IOException {
    List<String> productIds = new ArrayList<>();
    for (int i = 0; i < amount; i++) {
      productIds.add(UUID.randomUUID().toString());
    }
    return createProducts(projectId, productIds, productTitle);
  }

  public static List<Product> createProducts(
      String projectId, List<String> productIds, String productTitle) throws IOException {
    BranchName branchName = BranchName.of(projectId, "global", "default_catalog", "default_branch");

    float price = 8.0f;
    float originalPrice = 12.0f;

    PriceInfo priceInfo =
        PriceInfo.newBuilder()
            .setPrice(price)
            .setOriginalPrice(originalPrice)
            .setCurrencyCode("USD")
            .build();

    Product generatedProduct =
        Product.newBuilder()
            .setTitle(productTitle)
            .setType(Type.PRIMARY)
            .addCategories("Beverages")
            .addBrands("Google")
            .setPriceInfo(priceInfo)
            .setAvailability(Availability.IN_STOCK)
            .build();
    List<Product> createdProducts = new ArrayList<>();
    try (ProductServiceClient serviceClient = ProductServiceClient.create()) {
      for (String productId : productIds) {
        CreateProductRequest createProductRequest =
            CreateProductRequest.newBuilder()
                .setProduct(generatedProduct)
                .setProductId(productId)
                .setParent(branchName.toString())
                .build();

        createdProducts.add(serviceClient.createProduct(createProductRequest));
      }
    }
    return createdProducts;
  }

  public static void deleteProducts(List<Product> products) throws IOException {
    try (ProductServiceClient serviceClient = ProductServiceClient.create()) {
      for (Product product : products) {
        DeleteProductRequest deleteProductRequest =
            DeleteProductRequest.newBuilder().setName(product.getName()).build();

        serviceClient.deleteProduct(deleteProductRequest);
      }
    }
  }

  public static void waitForProductsToBeReadyToTest(
      String projectId, List<Product> products, String productTitle)
      throws InterruptedException, IOException {
    try (SearchServiceClient searchServiceClient = SearchServiceClient.create()) {
      ServingConfigName servingConfigName =
          ServingConfigName.of(projectId, "global", "default_catalog", "default_search");
      BranchName branchName =
          BranchName.of(projectId, "global", "default_catalog", "default_branch");
      SearchRequest searchRequest =
          SearchRequest.newBuilder()
              .setPlacement(servingConfigName.toString())
              .setBranch(branchName.toString())
              .setVisitorId("test_visitor")
              .setQuery(productTitle)
              .setPageSize(products.size())
              .build();

      for (int i = 0; i < 3; i++) {
        Thread.sleep(10000);

        SearchPagedResponse response = searchServiceClient.search(searchRequest);
        if (response.getPage().getResponse().getResultsCount() == products.size()) {
          break;
        }
      }
    }
  }
}
