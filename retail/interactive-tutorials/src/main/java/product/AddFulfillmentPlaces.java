/*
 * Copyright 2022 Google LLC
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

package product;

// [START retail_add_fulfillment_places]

import static setup.SetupCleanup.createProduct;
import static setup.SetupCleanup.deleteProduct;
import static setup.SetupCleanup.getProduct;

import com.google.cloud.ServiceOptions;
import com.google.cloud.retail.v2.AddFulfillmentPlacesRequest;
import com.google.cloud.retail.v2.ProductServiceClient;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

public class AddFulfillmentPlaces {

  public static void main(String[] args) throws IOException, InterruptedException {
    String projectId = ServiceOptions.getDefaultProjectId();
    String generatedProductId = UUID.randomUUID().toString();
    String productName =
        String.format(
            "projects/%s/locations/global/catalogs/default_catalog/branches/0/products/%s",
            projectId, generatedProductId);

    createProduct(generatedProductId);
    addFulfillmentPlaces(productName, "store2");
    getProduct(productName);
    deleteProduct(productName);
  }

  public static void addFulfillmentPlaces(String productName, String placeId)
      throws IOException, InterruptedException {

    System.out.println("Add fulfilment places");

    AddFulfillmentPlacesRequest addFulfillmentPlacesRequest =
        AddFulfillmentPlacesRequest.newBuilder()
            .setProduct(productName)
            .setType("pickup-in-store")
            .addPlaceIds(placeId)
            .setAllowMissing(true)
            .build();

    // To send an out-of-order request assign the invalid AddTime here:
    // Instant instant = LocalDateTime.now().minusDays(1).toInstant(ZoneOffset.UTC);
    // Timestamp previousDay = Timestamp.newBuilder()
    //          .setSeconds(instant.getEpochSecond())
    //          .setNanos(instant.getNano())
    //          .build();
    // addFulfillmentPlacesRequest =
    // addFulfillmentPlacesRequest.toBuilder().setAddTime(previousDay).build();

    System.out.println("Add fulfillment request " + addFulfillmentPlacesRequest);

    // Initialize client that will be used to send requests. This client only
    // needs to be created once, and can be reused for multiple requests. After
    // completing all of your requests, call the "close" method on the client to
    // safely clean up any remaining background resources.
    try (ProductServiceClient serviceClient = ProductServiceClient.create()) {
      // This is a long-running operation and its result is not immediately
      // present with get operations,thus we simulate wait with sleep method.
      System.out.println("Waiting for operation to finish...");
      serviceClient.addFulfillmentPlacesAsync(addFulfillmentPlacesRequest).getPollingFuture().get();
    } catch (ExecutionException e) {
      System.out.printf("Exception occurred during longrunning operation: %s%n", e.getMessage());
    }
  }
}

// [END retail_add_fulfillment_places]
