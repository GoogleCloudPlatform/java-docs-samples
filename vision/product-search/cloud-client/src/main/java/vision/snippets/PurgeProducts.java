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

package vision.snippets;

import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.vision.v1.LocationName;
import com.google.cloud.vision.v1.ProductSearchClient;
import com.google.cloud.vision.v1.PurgeProductsRequest;

import java.util.concurrent.TimeUnit;

// [START vision_product_search_purge_orphan_products]
public class PurgeProducts {

  /**
   * Delete the product and all its reference images.
   *
   * @param projectId - Id of the project.
   * @param computeRegion - A compute region name.
   * @param force - Perform the purge only when force is set to True.
   * @throws Exception - on I/O and API errors.
   */
  public static void purgeOrphanProducts(String projectId, String computeRegion, boolean force)
          throws Exception {
    try (ProductSearchClient client = ProductSearchClient.create()) {

      String parent = LocationName.format(projectId, computeRegion);

      // The purge operation is async.
      PurgeProductsRequest req = PurgeProductsRequest
              .newBuilder()
              .setDeleteOrphanProducts(true)
              .setForce(force)
              .setParent(parent)
              .build();

      OperationFuture response = client.purgeProductsAsync(req);
      response.getPollingFuture().get(90, TimeUnit.SECONDS);

      System.out.println("Orphan products deleted.");
    }
  }
}
// [END vision_product_search_purge_orphan_products]