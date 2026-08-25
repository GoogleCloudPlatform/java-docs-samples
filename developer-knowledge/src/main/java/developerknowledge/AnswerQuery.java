/*
 * Copyright 2026 Google LLC
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

package developerknowledge;

// [START developerknowledge_answer_query]
import com.google.developers.knowledge.v1.AnswerQueryRequest;
import com.google.developers.knowledge.v1.AnswerQueryResponse;
import com.google.developers.knowledge.v1.DeveloperKnowledgeClient;
import java.io.IOException;

public class AnswerQuery {

  public static void main(String[] args) throws IOException {
    String query =
        args.length > 0 ? args[0] : "How do I create a Google Cloud Storage bucket?";
    answerQuery(query);
  }

  /**
   * Answers a developer question grounded in Google developer documentation.
   *
   * @param query The technical question to answer.
   */
  public static AnswerQueryResponse answerQuery(String query) throws IOException {
    try (DeveloperKnowledgeClient client = DeveloperKnowledgeClient.create()) {
      AnswerQueryRequest request =
          AnswerQueryRequest.newBuilder().setQuery(query).build();

      AnswerQueryResponse response = client.answerQuery(request);

      System.out.println("Answer:\n" + response.getAnswer().getAnswerText() + "\n");
      System.out.println("Citations count: " + response.getAnswer().getCitationsCount());
      System.out.println("References count: " + response.getAnswer().getReferencesCount());

      return response;
    }
  }
}
// [END developerknowledge_answer_query]
