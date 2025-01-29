package app;

import java.util.List;
import org.json.JSONObject;

public class LeaderboardResponse {

  private Long position;
  private List<LeaderboardEntry> entries;
  private int fromCache;

  public LeaderboardResponse(
    Long position,
    List<LeaderboardEntry> entries,
    int fromCache
  ) {
    this.position = position;
    this.entries = entries;
    this.fromCache = fromCache;
  }

  public Long getPosition() {
    return position;
  }

  public List<LeaderboardEntry> getEntries() {
    return entries;
  }

  public int getFromCache() {
    return fromCache;
  }

  public JSONObject toJson() {
    JSONObject json = new JSONObject();
    json.put("position", position);
    json.put("entries", entries);
    json.put("fromCache", fromCache);
    return json;
  }
}
