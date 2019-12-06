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

package com.example.recommender;

// [START recommender_list_recommendations]
import com.google.cloud.recommender.v1beta1.ListRecommendationsRequest;
import com.google.cloud.recommender.v1beta1.Recommendation;
import com.google.cloud.recommender.v1beta1.RecommenderClient;
import java.io.IOException;

public class ListRecommendations {

  // List recommendations for a specified project, location, and recommender
  public static void listRecommendations(String projectId, String location, String recommender) {
    try (RecommenderClient recommenderClient = RecommenderClient.create()) {
      String parent =
          String.format(
              "projects/%s/locations/%s/recommenders/%s", projectId, location, recommender);
      ListRecommendationsRequest request =
          ListRecommendationsRequest.newBuilder().setParent(parent).build();
      for (Recommendation responseItem :
          recommenderClient.listRecommendations(request).iterateAll()) {
        Recommendation recommendation = responseItem;
        System.out.println("Recommendation name: " + recommendation.getName());
        System.out.println("- description: " + recommendation.getDescription());
        System.out.println(
            "- primary_impact.category: " + recommendation.getPrimaryImpact().getCategory());
        System.out.println("- state_info.state: " + recommendation.getStateInfo().getState());
        System.out.println();
      }
      System.out.println("List recommendations successful");
    } catch (IOException e) {
      System.out.println("Unable to initialize recommender client: \n" + e.toString());
    }
  }

  public static void main(String... args) {
    String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
    String location = args[0];
    String recommender = args[1];

    listRecommendations(projectId, location, recommender);
  }
}
// [END recommender_list_recommendations]
