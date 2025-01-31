package app;

import java.util.List;
import org.json.JSONObject;

public class LeaderboardResponse {

  private List<LeaderboardEntry> entries;
  private int fromCache;

  public LeaderboardResponse(List<LeaderboardEntry> entries, int fromCache) {
    this.entries = entries;
    this.fromCache = fromCache;
  }

  public List<LeaderboardEntry> getEntries() {
    return entries;
  }

  public int getFromCache() {
    return fromCache;
  }

  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("entries", entries);
    json.put("fromCache", fromCache);
    return json;
  }
}
