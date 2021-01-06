/*
 * Copyright 2021 Google LLC
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

package com.example.cloudrun;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

@Controller
public final class VoteController {

  private static final Logger logger = LoggerFactory.getLogger(VoteController.class);
  private final JdbcTemplate jdbcTemplate;

  public VoteController(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @GetMapping("/")
  public String index(Model model) {
    try {
      // Query the total count of "CATS" from the database.
      int catVotes = getVoteCount("CATS");
      // Query the total count of "DOGS" from the database.
      int dogVotes = getVoteCount("DOGS");

      // Calculate and set leader values.
      String leadTeam;
      String leaderMessage;
      int voteDiff = 0;
      if (catVotes != dogVotes) {
        if (catVotes > dogVotes) {
          leadTeam = "CATS";
          voteDiff = catVotes - dogVotes;
        } else {
          leadTeam = "DOGS";
          voteDiff = dogVotes - catVotes;
        }
        String append = (voteDiff > 1) ? "s" : "";
        leaderMessage = leadTeam + " are winning by " + voteDiff + " vote" + append + ".";
      } else {
        leaderMessage = "CATS and DOGS are evenly matched!";
        leadTeam = null;
      }
      
      // Query the last 5 votes from the database.
      List<Vote> votes = getVotes();

      // Add values to template
      model.addAttribute("leaderMessage", leaderMessage);
      model.addAttribute("leadTeam", leadTeam);
      model.addAttribute("catVotes", catVotes);
      model.addAttribute("dogVotes", dogVotes);
      model.addAttribute("votes", votes);
    } catch (DataAccessException e) {
      String message =
          "Error while connecting to the Cloud SQL database. "
              + "Check that your username and password are correct, that the Cloud SQL "
              + "proxy is running (locally), and that the database/table exists and is "
              + "ready for use: "
              + e.toString();
      logger.error(message);
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Unable to load page; see logs for more details.", e);
    }
    return "index";
  }

  @PostMapping("/")
  @ResponseBody
  public String vote(
      @RequestHeader Map<String, String> headers, @RequestParam Map<String, String> body) {
    // Get decoded Id Platform user id
    String uid = authenticateJwt(headers);
    // Get the team from the request and record the time of the vote.
    String team = body.get("team");
    Date date = new Date();
    Timestamp timestamp = new Timestamp(date.getTime());

    // Validate team selection
    if (team == null || (!team.equals("CATS") && !team.equals("DOGS"))) {
      return "error: '" + team + "' is not a valid candidate.";
    }

    // Create a vote record to be stored in the database.
    Vote vote = new Vote(uid, team, timestamp);
    // Save the data to the database.
    try {
      insertVote(vote);
      MDC.put("uid", uid);
      MDC.put("team", team);
      logger.info("vote_inserted");
    } catch (DataAccessException e) {
      logger.error("Error while attempting to submit vote: " + e.toString());
      throw new ResponseStatusException(
          HttpStatus.INTERNAL_SERVER_ERROR, "Unable to cast vote; see logs for more details.", e);
    }
    return "Successfully voted for " + team + " at " + timestamp.toLocalDateTime();
  }

  /** Extract and verify Id Token from header */
  private String authenticateJwt(Map<String, String> headers) {
    String authHeader = headers.get("Authorization");
    if (authHeader != null) {
      String idToken = authHeader.split(" ")[1];
      // If the provided ID token has the correct format, is not expired, and is
      // properly signed, the method returns the decoded ID token
      try {
        FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(idToken);
        String uid = decodedToken.getUid();
        return uid;
      } catch (FirebaseAuthException e) {
        logger.error("Error with authentication: " + e.toString());
        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "", e);
      }
    } else {
      logger.error("Error no authorization header");
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
    }
  }

  /** Insert a vote record into the database. */
  public void insertVote(Vote vote) throws DataAccessException {
    this.jdbcTemplate.update(
        "INSERT INTO votes(candidate, time_cast, uid) VALUES(?,?,?)",
        vote.getCandidate(),
        vote.getTimeCast(),
        vote.getUid());
  }

  /** Retrieve the latest 5 vote records from the database. */
  public List<Vote> getVotes() throws DataAccessException {
    return this.jdbcTemplate.query(
        "SELECT candidate, time_cast, uid FROM votes ORDER BY time_cast DESC LIMIT 5",
        (rs, rowNum) -> {
          String candidate = rs.getString("candidate");
          String uid = rs.getString("uid");
          Timestamp timeCast = rs.getTimestamp("time_cast");
          return new Vote(uid, candidate, timeCast);
        });
  }

  /** Retrieve the total count of records for a given candidate from the database. */
  public int getVoteCount(String candidate) throws DataAccessException {
    return this.jdbcTemplate.queryForObject(
        "SELECT COUNT(vote_id) FROM votes WHERE candidate = ?",
        Integer.class,
        new Object[] {candidate});
  }
}
