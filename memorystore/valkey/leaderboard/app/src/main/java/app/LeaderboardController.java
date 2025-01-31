/*
 * Copyright 2025 Google LLC
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

/**
 * The API controller for the leaderboard application.
 *
 * <p>The controller contains two routes: - GET /api/leaderboard - By default, this endpoint returns
 * the top X entries in the leaderboard. Optionally, a parameter `position` can be provided to
 * return the leaderboard starting from that position. - POST /api/leaderboard - This endpoint
 * creates or updates a leaderboard entry with a given username and score.
 */
package app;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private DataController dataController;

    public LeaderboardController(DataController dataController) {
        this.dataController = dataController;
    }

    @GetMapping
    public ResponseEntity<String> getLeaderboard(
            @RequestParam(required = true) Long position,
            @RequestParam(required = true) Integer size,
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String username) {
        // If the position is not provided, set position as 0
        if (position == null) {
            position = 0L;
        }

        // Throw an error if the position is negative
        if (position < 0) {
            return ResponseEntity.badRequest().body("Position must be a positive number");
        }

        // default to descending order if orderBy is not provided
        if (orderBy != null && !OrderByType.isValid(orderBy)) {
            orderBy = OrderByType.HIGH_TO_LOW.toString();
        }

        // default to 10 entries if size is not provided
        if (size == null) {
            size = 10;
        }

        // Throw an error if the size is negative
        if (size < 1) {
            return ResponseEntity.badRequest().body("Size must be a positive number");
        }

        // Get the Leaderboard entries
        LeaderboardResponse response =
                dataController.getLeaderboard(
                        position, OrderByType.fromString(orderBy), size, username);

        // Return the response as a JSON string
        return ResponseEntity.ok(response.toJson().toString());
    }

    @PostMapping
    public ResponseEntity<String> create(@RequestBody LeaderboardEntry entry) {
        // extract the parameters from the request body
        String username = entry.getUsername();
        Double score = entry.getScore();

        // Check if the username and score have been provided
        if (username.isEmpty() || score == null) {
            // Return an error message
            return ResponseEntity.badRequest().body("Score and username are required");
        }

        // Create or update the entry
        dataController.createOrUpdate(username, score);

        // return a success message
        return ResponseEntity.ok("Entry created");
    }
}
