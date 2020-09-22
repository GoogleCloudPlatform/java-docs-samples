package com.example.cloudrun;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;

/** Defines a controller to handle HTTP requests */
@Controller
public final class IndexController {

  private static final Logger logger = LoggerFactory.getLogger(IndexController.class);
  private final JdbcTemplate jdbcTemplate;

  public IndexController(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @GetMapping("/")
  public String index(Model model) {
    try {
      int catVotes = getVoteCount("CATS");
      int dogVotes = getVoteCount("DOGS");
      List<Vote> votes = getVotes();
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
      model.addAttribute("leaderMessage", leaderMessage);
      model.addAttribute("leadTeam", leadTeam);
      model.addAttribute("catVotes", catVotes);
      model.addAttribute("dogVotes", dogVotes);
      model.addAttribute("votes", votes);
    } catch (Exception e) {
      logger.error(e.toString());
    }
    return "index";
  }

  @PostMapping("/")
  @ResponseBody
  public String vote(@RequestParam Map<String, String> body) {
    String team = body.get("team");
    String uid = "test";
    Date date = new Date();
    Timestamp timestamp = new Timestamp(date.getTime());
    if (team == null || (!team.equals("CATS") && !team.equals("DOGS"))) {
      return "error: '" + team + "' is not a valid candidate.";
    }
    Vote vote = new Vote(uid, team, timestamp);
    try {
      insertVote(vote);
      logger.info("vote inserted: " + vote.toString());
    } catch (Exception e) {
      logger.error("error while attempting to submit vote: %s", e);
    }
    return "Successfully voted for " + team + " at " + timestamp.toLocalDateTime();
  }


  public void insertVote(Vote vote) {
    this.jdbcTemplate.update(
      "INSERT INTO votes(candidate, time_cast, uid) VALUES(?,?,?)", 
      vote.getCandidate(), vote.getTimeCast(), vote.getUid());
  }

  public List<Vote> getVotes() {
    return this.jdbcTemplate.query(
      "SELECT candidate, time_cast, uid FROM votes ORDER BY time_cast DESC LIMIT 5", 
      (rs, rowNum) -> {
        String candidate = rs.getString("candidate");
        String uid = rs.getString("uid");
        Timestamp timeCast = rs.getTimestamp("time_cast");
        return new Vote(uid, candidate, timeCast);
      });
  }

  public int getVoteCount(String candidate) {
    return this.jdbcTemplate.queryForObject(
        "SELECT COUNT(vote_id) FROM votes WHERE candidate = ?",
        new Object[] {candidate},
        Integer.class);
  }
}
