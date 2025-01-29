/**
 * The API controller for the leaderboard application.
 *
 * The controller contains two routes:
 * - GET /api/leaderboard - By default, this endpoint returns the top X entries in the leaderboard. Optionally, a parameter `position` can be provided to return the leaderboard starting from that position.
 * - POST /api/leaderboard - This endpoint creates or updates a leaderboard entry with a given username and score.
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
  public ResponseEntity<String> readAt(
    @RequestParam(required = false) Long position
  ) {
    // If the position is not provided, set position as 0
    if (position == null) {
      position = 0L;
    }

    if (position < 0) {
      return ResponseEntity.badRequest()
        .body("Position must be a positive number");
    }

    // Get a portion of the leaderboard
    LeaderboardResponse response = dataController.getAt(position);

    return ResponseEntity.ok(response.toJson().toString());
  }

  @PostMapping
  public ResponseEntity<String> create(@RequestBody LeaderboardEntry entry) {
    String username = entry.getUsername();
    Double score = entry.getScore();

    // Check if the username and score have been provided
    if (username.isEmpty() || score == null) {
      // Return an error message
      return ResponseEntity.badRequest()
        .body("Score and username are required");
    }

    Double existingScore = dataController.getScore(username);
    if (existingScore != null && score <= existingScore) {
      return ResponseEntity.badRequest()
        .body("New score not higher than existing score");
    }

    // Create or update the entry
    dataController.createOrUpdate(username, score);

    return ResponseEntity.ok("Entry created");
  }
}
