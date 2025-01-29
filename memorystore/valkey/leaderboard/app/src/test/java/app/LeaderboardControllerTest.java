package app;

import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(LeaderboardController.class)
class LeaderboardControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @MockBean
  private DataController dataController;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  @DisplayName(
    "Test reading the leaderboard with default position (position=0)"
  )
  void testReadAt_DefaultPosition() throws Exception {
    // Given DataController returns two leaderboard entries when position = 0
    List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();
    leaderboardEntries.add(new LeaderboardEntry("user1", 100.0));
    leaderboardEntries.add(new LeaderboardEntry("user2", 90.0));
    LeaderboardResponse response = new LeaderboardResponse(
      0L,
      leaderboardEntries,
      2
    );

    given(dataController.getAt(0L)).willReturn(response);

    // Perform GET /api/leaderboard with no position parameter
    mockMvc
      .perform(get("/api/leaderboard"))
      .andExpect(status().isOk())
      .andExpect(content().string(response.toJson().toString()))
      .andExpect(content().string(Matchers.containsString("\"position\":0")))
      .andExpect(
        content()
          .string(
            Matchers.containsString("{\"score\":100,\"username\":\"user1\"}")
          )
      )
      .andExpect(
        content()
          .string(
            Matchers.containsString("{\"score\":90,\"username\":\"user2\"}")
          )
      );

    // Verify that getAt was called with position=0
    verify(dataController).getAt(0L);
  }

  @Test
  @DisplayName("Test reading the leaderboard at a specific position")
  void testReadAt_WithPosition() throws Exception {
    // Given DataController returns two leaderboard entries when position = 5
    List<LeaderboardEntry> leaderboardEntries = new ArrayList<>();
    leaderboardEntries.add(new LeaderboardEntry("user6", 50.0));
    leaderboardEntries.add(new LeaderboardEntry("user7", 45.0));
    LeaderboardResponse response = new LeaderboardResponse(
      5L,
      leaderboardEntries,
      2
    );

    given(dataController.getAt(5L)).willReturn(response);

    // Perform GET /api/leaderboard?position=5
    mockMvc
      .perform(get("/api/leaderboard").param("position", "5"))
      .andExpect(status().isOk())
      .andExpect(content().string(response.toJson().toString()))
      .andExpect(content().string(Matchers.containsString("\"position\":5")))
      .andExpect(
        content()
          .string(
            Matchers.containsString("{\"score\":50,\"username\":\"user6\"}")
          )
      )
      .andExpect(
        content()
          .string(
            Matchers.containsString("{\"score\":45,\"username\":\"user7\"}")
          )
      );

    // Verify that getAt was called with position=5
    verify(dataController).getAt(5L);
  }

  @Test
  @DisplayName(
    "Test that the leaderboard cannot be read at a negative position"
  )
  void testReadAt_NegativePosition() throws Exception {
    // Perform GET /api/leaderboard?position=-1
    mockMvc
      .perform(get("/api/leaderboard").param("position", "-1"))
      .andExpect(status().isBadRequest())
      .andExpect(content().string("Position must be a positive number"));

    // Verify that getAt was never called
    verify(dataController, never()).getAt(anyLong());
  }

  @Test
  @DisplayName("Test reading the leaderboard returns empty if no entries found")
  void testReadAt_EmptyList() throws Exception {
    // Given DataController returns an empty list for position=10
    given(dataController.getAt(10L)).willReturn(
      new LeaderboardResponse(10L, new ArrayList<>(), 0)
    );

    // Perform GET /api/leaderboard?position=10
    mockMvc
      .perform(get("/api/leaderboard").param("position", "10"))
      .andExpect(status().isOk())
      // Expect the response to be an empty string (or no entries)
      .andExpect(content().string(Matchers.containsString("\"position\":10")))
      .andExpect(content().string(Matchers.containsString("\"entries\":[]")))
      .andExpect(content().string(Matchers.containsString("\"fromCache\":0")));

    // Verify that getAt was called with position=10
    verify(dataController).getAt(10L);
  }

  @Test
  @DisplayName(
    "Test creating a new score when it is higher than existing score"
  )
  void testCreate_SuccessNewHighScore() throws Exception {
    LeaderboardEntry entry = new LeaderboardEntry("user1", 150.0);

    // Suppose user1's old score is 100
    given(dataController.getScore("user1")).willReturn(100.0);

    // Perform POST /api/leaderboard
    mockMvc
      .perform(
        post("/api/leaderboard")
          .contentType("application/json")
          .content(objectMapper.writeValueAsString(entry))
      )
      .andExpect(status().isOk())
      .andExpect(content().string("Entry created"));

    // Verify we called getScore and then createOrUpdate
    verify(dataController).getScore("user1");
    verify(dataController).createOrUpdate("user1", 150.0);
  }

  @Test
  @DisplayName(
    "Test that a score is not created if it is not higher than the existing score"
  )
  void testCreate_ScoreNotHigher() throws Exception {
    LeaderboardEntry entry = new LeaderboardEntry("user1", 50.0);

    // Suppose user1 already has 100
    given(dataController.getScore("user1")).willReturn(100.0);

    // Perform POST /api/leaderboard
    mockMvc
      .perform(
        post("/api/leaderboard")
          .contentType("application/json")
          .content(objectMapper.writeValueAsString(entry))
      )
      .andExpect(status().isBadRequest())
      .andExpect(content().string("New score not higher than existing score"));

    // Verify createOrUpdate was never called
    verify(dataController, never()).createOrUpdate(anyString(), anyDouble());
  }

  @Test
  @DisplayName("Test that username and score are required")
  void testCreate_EmptyUsernameOrNoScore() throws Exception {
    // 1) Empty username
    LeaderboardEntry entry1 = new LeaderboardEntry("", 100.0);
    mockMvc
      .perform(
        post("/api/leaderboard")
          .contentType("application/json")
          .content(objectMapper.writeValueAsString(entry1))
      )
      .andExpect(status().isBadRequest())
      .andExpect(content().string("Score and username are required"));

    // 2) Null score
    LeaderboardEntry entry2 = new LeaderboardEntry("user2", null);
    mockMvc
      .perform(
        post("/api/leaderboard")
          .contentType("application/json")
          .content(objectMapper.writeValueAsString(entry2))
      )
      .andExpect(status().isBadRequest())
      .andExpect(content().string("Score and username are required"));

    // Verify that createOrUpdate was never called in either case
    verify(dataController, never()).createOrUpdate(anyString(), anyDouble());
  }

  @Test
  @DisplayName("Test creating a new entry for a first-time user")
  void testCreate_FirstTimeUser() throws Exception {
    LeaderboardEntry entry = new LeaderboardEntry("newUser", 120.0);

    // Suppose user doesn't exist in the system
    given(dataController.getScore("newUser")).willReturn(null);

    // Perform POST /api/leaderboard
    mockMvc
      .perform(
        post("/api/leaderboard")
          .contentType("application/json")
          .content(objectMapper.writeValueAsString(entry))
      )
      .andExpect(status().isOk())
      .andExpect(content().string("Entry created"));

    verify(dataController).createOrUpdate("newUser", 120.0);
  }
}
