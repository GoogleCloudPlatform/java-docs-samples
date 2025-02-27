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
* <p>The controller contains two routes:
* - GET /api/leaderboard - Returns top X entries in the leaderboard. Optional
*   position parameter returns entries starting from that position.
* - POST /api/leaderboard - Creates or updates a leaderboard entry with given
*   username and score.
*/

package app;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

  /** The data controller for managing leaderboard entries. */
  private final DataController dataController;
  /** Default number of entries to return per page. */
  private static final int DEFAULT_PAGE_SIZE = 10;

  /**
   * Constructs a new LeaderboardController.
   *
   * @param controller The data controller to use
   */
  public LeaderboardController(final DataController controller) {
    this.dataController = controller;
  }

  /**
   * Gets the leaderboard entries.
   *
   * @param position Starting position in the leaderboard
   * @param size   Number of entries to return
   * @param orderBy  Sort order (HIGH_TO_LOW or LOW_TO_HIGH)
   * @param username Filter results by username
   * @return ResponseEntity containing JSON string of leaderboard entries
   */
  @GetMapping
  public ResponseEntity<String> getLeaderboard(
      @RequestParam(required = true) final Long position,
      @RequestParam(required = true) final Integer size,
      @RequestParam(required = false) final String orderBy,
      @RequestParam(required = false) final String username) {
    // If the position is not provided, set position as 0
    Long pos = position;
    if (pos == null) {
      pos = 0L;
    }

    // Throw an error if the position is negative
    if (pos < 0) {
      return ResponseEntity.badRequest()
          .body("Position must be a positive number");
    }

    // default to descending order if orderBy is not provided
    String order = orderBy;
    if (order != null && !OrderByType.isValid(order)) {
      order = OrderByType.HIGH_TO_LOW.toString();
    }

    // default to DEFAULT_PAGE_SIZE entries if size is not provided
    Integer pageSize = size;
    if (pageSize == null) {
      pageSize = Integer.valueOf(DEFAULT_PAGE_SIZE);
    }

    // Throw an error if the size is negative
    if (pageSize < 1) {
      return ResponseEntity.badRequest()
          .body("Size must be a positive number");
    }

    // Get the Leaderboard entries
    LeaderboardResponse response = dataController.getLeaderboard(
        pos, OrderByType.fromString(order), pageSize, username);

    // Return the response as a JSON string
    return ResponseEntity.ok(response.toJson().toString());
  }

  /**
   * Creates or updates a leaderboard entry.
   *
   * @param entry The leaderboard entry to create/update
   * @return ResponseEntity with status and message
   */
  @PostMapping
  public ResponseEntity<String> create(
      @RequestBody final LeaderboardEntry entry) {
    // extract the parameters from the request body
    String username = entry.getUsername();
    Double score = entry.getScore();

    // Check if the username and score have been provided
    if (username.isEmpty() || score == null) {
      return ResponseEntity.badRequest()
          .body("Score and username are required");
    }

    // Create or update the entry
    dataController.createOrUpdate(username, score);

    // return a success message
    return ResponseEntity.ok("Entry created");
  }
}
