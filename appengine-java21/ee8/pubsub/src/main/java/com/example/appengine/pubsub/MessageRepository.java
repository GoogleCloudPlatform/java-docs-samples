/*
 * Copyright 2017 Google LLC
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

package com.example.appengine.pubsub;

import java.util.List;

public interface MessageRepository {

  /** Save message to persistent storage. */
  void save(Message message);

  /**
   * Retrieve most recent stored messages.
   *
   * @param limit number of messages
   * @return list of messages
   */
  List<Message> retrieve(int limit);

  /** Save claim to persistent storage. */
  void saveClaim(String claim);

  /**
   * Retrieve most recent stored claims.
   *
   * @param limit number of messages
   * @return list of claims
   */
  List<String> retrieveClaims(int limit);

  /** Save token to persistent storage. */
  void saveToken(String token);

  /**
   * Retrieve most recent stored tokens.
   *
   * @param limit number of messages
   * @return list of tokens
   */
  List<String> retrieveTokens(int limit);
}
