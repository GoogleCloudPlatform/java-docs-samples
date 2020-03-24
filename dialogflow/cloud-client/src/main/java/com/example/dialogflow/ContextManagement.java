/*
 * Copyright 2018 Google LLC
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

package com.example.dialogflow;

// Imports the Google Cloud client library

import com.google.cloud.dialogflow.v2.Context;
import com.google.cloud.dialogflow.v2.ContextName;
import com.google.cloud.dialogflow.v2.ContextsClient;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.common.collect.Lists;
import com.google.protobuf.Value;

import java.util.List;
import java.util.Map.Entry;

/**
 * DialogFlow API Context sample.
 */
public class ContextManagement {
  // [START dialogflow_create_context]

  /**
   * Create an entity type with the given display name
   *
   * @param contextId     The Id of the context.
   * @param sessionId     Identifier of the DetectIntent session.
   * @param lifespanCount The lifespan count of the context.
   * @param projectId     Project/Agent Id.
   * @return The new Context.
   */
  public static Context createContext(
      String contextId,
      String sessionId,
      String projectId,
      int lifespanCount) throws Exception {
    // Instantiates a client
    try (ContextsClient contextsClient = ContextsClient.create()) {
      // Set the session name using the sessionId (UUID) and projectID (my-project-id)
      SessionName session = SessionName.of(projectId, sessionId);

      // Create the context name with the projectId, sessionId, and contextId
      ContextName contextName = ContextName.newBuilder()
          .setProject(projectId)
          .setSession(sessionId)
          .setContext(contextId)
          .build();

      // Create the context with the context name and lifespan count
      Context context = Context.newBuilder()
          .setName(contextName.toString()) // The unique identifier of the context
          .setLifespanCount(lifespanCount) // Number of query requests before the context expires.
          .build();

      // Performs the create context request
      Context response = contextsClient.createContext(session, context);
      System.out.format("Context created: %s\n", response);

      return response;
    }
  }
  // [END dialogflow_create_context]

  // [START dialogflow_delete_context]

  /**
   * Delete entity type with the given entity type name
   *
   * @param contextId The Id of the context.
   * @param sessionId Identifier of the DetectIntent session.
   * @param projectId Project/Agent Id.
   */
  public static void deleteContext(String contextId, String sessionId, String projectId)
      throws Exception {
    // Instantiates a client
    try (ContextsClient contextsClient = ContextsClient.create()) {
      // Create the context name with the projectId, sessionId, and contextId
      ContextName contextName = ContextName.of(projectId, sessionId, contextId);
      // Performs the delete context request
      contextsClient.deleteContext(contextName);
    }
  }
  // [END dialogflow_delete_context]
}
