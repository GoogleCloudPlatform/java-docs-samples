/*
 * Copyright 2024 Google LLC
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
 *
 *
 * Create features in bulk for an existing entity type. See
 * https://cloud.google.com/vertex-ai/docs/featurestore/setup
 * before running the code snippet
 */

package com.example;

// [START routeoptimization_v1_OptimizeTours_sync]
import com.google.maps.routeoptimization.v1.OptimizeToursRequest;
import com.google.maps.routeoptimization.v1.OptimizeToursResponse;
import com.google.maps.routeoptimization.v1.RouteOptimizationClient;
import com.google.maps.routeoptimization.v1.Shipment;
import com.google.maps.routeoptimization.v1.Shipment.VisitRequest;
import com.google.maps.routeoptimization.v1.ShipmentModel;
import com.google.maps.routeoptimization.v1.Vehicle;
import com.google.type.LatLng;

public class OptimizeTours {
  // [END routeoptimization_v1_OptimizeTours_sync]
  public static void main(String[] args) throws Exception {
    // TODO(developer): Replace these variables before running the sample.
    String project = "YOUR_PROJECT_ID";
    System.out.println(optimizeTours(project));
  }

  // [START routeoptimization_v1_OptimizeTours_sync]
  public static OptimizeToursResponse optimizeTours(String projectId) throws Exception {
    RouteOptimizationClient client = RouteOptimizationClient.create();
    OptimizeToursRequest request =
        OptimizeToursRequest.newBuilder()
            .setParent("projects/" + projectId)
            .setModel(
                ShipmentModel.newBuilder()
                    .addShipments(
                        Shipment.newBuilder()
                            .addPickups(
                                VisitRequest.newBuilder()
                                    .setArrivalLocation(
                                        LatLng.newBuilder().setLatitude(48.8).setLongitude(2.4))))
                    .addVehicles(
                        Vehicle.newBuilder()
                            .setStartLocation(
                                LatLng.newBuilder().setLatitude(48.9).setLongitude(2.5))))
            .build();
    return client.optimizeTours(request);
  }
}
// [END routeoptimization_v1_OptimizeTours_sync]
