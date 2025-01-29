/**
 * Utility class providing helper methods for the Leaderboard app.
 */

package app;

import java.util.List;
import java.util.stream.IntStream;
import org.json.JSONArray;
import org.json.JSONObject;

public class Utils {

  public static JSONObject convertToLeaderboardJson(
    List<LeaderboardEntry> leaderboardList,
    long position
  ) {
    // Create the main JSON object
    JSONObject result = new JSONObject();

    // Add the position to the JSON object
    result.put("position", position);

    // Create a JSON array to store leaderboard entries
    JSONArray leaderboardArray = new JSONArray();

    // Iterate through the leaderboard list and convert each entry to JSON
    for (LeaderboardEntry entry : leaderboardList) {
      JSONObject entryJson = new JSONObject();
      entryJson.put("username", entry.getUsername());
      entryJson.put("score", entry.getScore());
      leaderboardArray.put(entryJson);
    }

    // Add the leaderboard array to the main JSON object
    result.put("leaderboard", leaderboardArray);

    // Convert the JSON object to a string and return
    return result;
  }
}
