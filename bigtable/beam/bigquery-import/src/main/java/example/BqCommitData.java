/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

// [START bigquery_my_data]
package example;

import com.google.api.services.bigquery.model.TableRow;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.beam.sdk.coders.AvroCoder;
import org.apache.beam.sdk.coders.DefaultCoder;


public class BqCommitData {

  @SuppressFBWarnings("URF_UNREAD_FIELD")
  static class MyStruct {

    String stringValue;
    Long int64Value;
  }

  @DefaultCoder(AvroCoder.class)
  public static class CommitData {
        /*
Rowkey
  email#repo#commit
Store
  email
  repo
  commit
  author name
  committer name
  subject
  message
     */

    String authorEmail;
    String authorName;
    String commit;
    Long commitDateSeconds;
    String committerName;
    String message;
    String repo;
    String subject;










    public static CommitData fromTableRow(TableRow row) {
      CommitData commit = new CommitData();

      // System.out.println("in fromtablerow");
      // System.out.println("row = " + row);
      Map<String, Object> authorValues = (Map<String, Object>) row.get("author");
      Map<String, Object> committerValues = (Map<String, Object>) row.get("committer");

      commit.authorEmail = String.valueOf(authorValues.get("email"));
      commit.authorName = String.valueOf(authorValues.get("name"));
      commit.commit = String.valueOf(row.get("commit"));
      Object seconds = ((Map<String, Object>) committerValues.get("date")).get("seconds");
      commit.commitDateSeconds = seconds != null ? Long.parseLong(String.valueOf(seconds)) : null;
      commit.committerName = String.valueOf(committerValues.get("name"));
      commit.message = String.valueOf(row.get("message"));
      ArrayList<String> repo_name = (ArrayList<String>) row.get("repo_name");
      commit.repo = String.valueOf(repo_name.get(0).split("/")[1]);
      commit.subject = String.valueOf(row.get("subject"));
      // commit.commitDateSeconds = Long.parseLong((String) row.get("committer.date"));

      // commit.myStruct.stringValue = (String) structValues.get("string_value");
      // commit.myStruct.int64Value = Long.parseLong((String) structValues.get("int64_value"));

      return commit;
    }

    public String getRowkey() {
      return String.format("%s#%s#%s", this.authorEmail, this.repo, this.commit);
    }
  }
}
// [END bigquery_my_data]