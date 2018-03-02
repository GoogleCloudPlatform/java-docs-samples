/*
 * Copyright 2018 Google Inc.
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

package com.example.containeranalysis;

import static java.lang.Thread.sleep;

import com.google.cloud.devtools.containeranalysis.v1alpha1.ContainerAnalysisClient;
import com.google.cloud.devtools.containeranalysis.v1alpha1.PagedResponseWrappers.ListNoteOccurrencesPagedResponse;
import com.google.cloud.devtools.containeranalysis.v1alpha1.PagedResponseWrappers.ListOccurrencesPagedResponse;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.containeranalysis.v1alpha1.ListOccurrencesRequest;
import com.google.containeranalysis.v1alpha1.Note;
import com.google.containeranalysis.v1alpha1.Occurrence;
import com.google.containeranalysis.v1alpha1.VulnerabilityType;
import com.google.containeranalysis.v1alpha1.VulnerabilityType.VulnerabilityDetails;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.Subscription;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;

/**
 * API usage samples
 */
public class Samples {

  // [START create_note]
  /**
   * Creates and returns a new note
   *
   * @param noteId A user-specified identifier for the note.
   * @param projectId the GCP project the note will be created under
   * @return a Note object representing the new note
   * @throws Exception on errors while closing the client
   */
  public static Note createNote(String noteId, String projectId) throws Exception {
    Note.Builder noteBuilder = Note.newBuilder();
    noteBuilder.setVulnerabilityType(VulnerabilityType.newBuilder().build());
    Note newNote = noteBuilder.build();

    try (ContainerAnalysisClient client = ContainerAnalysisClient.create()) {
      final String projectName = client.formatProjectName(projectId);
      return client.createNote(projectName, noteId, newNote);
    }
  }
  // [END create_note]


  // [START create_occurrence]
  /**
   * Creates and returns a new occurrence
   *
   * @param imageUrl the Container Registry URL associated with the image
   *                 example: "https://gcr.io/project/image@sha256:foo"
   * @param parentNoteId the identifier of the note associated with this occurrence
   * @param projectId the GCP project the occurrence will be created under
   * @return an Occurrence object representing the new occurrence
   * @throws Exception on errors while closing the client
   */
  public static Occurrence createOccurrence(String imageUrl,
      String parentNoteId,
      String projectId) throws Exception {
    try (ContainerAnalysisClient client = ContainerAnalysisClient.create()) {
      final String parentNoteName = client.formatNoteName(projectId, parentNoteId);
      final String projectName = client.formatProjectName(projectId);

      Occurrence.Builder occBuilder = Occurrence.newBuilder();
      occBuilder.setNoteName(parentNoteName);
      occBuilder.setResourceUrl(imageUrl);
      VulnerabilityDetails vd = VulnerabilityType.VulnerabilityDetails.newBuilder().build();
      occBuilder.setVulnerabilityDetails(vd);
      Occurrence newOcc = occBuilder.build();
      return client.createOccurrence(projectName, newOcc);
    }
  }
  // [END create_occurrence]

  // [START update_note]
  /**
   * Makes an update to an existing note
   *
   * @param updated a Note object representing the desired updates to push
   * @param noteId the identifier of the existing note
   * @param projectId the GCP project the occurrence will be created under.
   * @throws Exception on errors while closing the client
   */
  public static void updateNote(Note updated, String noteId, String projectId) throws Exception {
    try (ContainerAnalysisClient client = ContainerAnalysisClient.create()) {
      final String noteName = client.formatNoteName(projectId, noteId);

      client.updateNote(noteName, updated);
    }
  }
  // [END update_note]

  // [START update_occurrence]
  /**
   * Makes an update to an existing occurrence
   *
   * @param occurrenceName the name of the occurrence to delete.
   *                       format: "projects/{projectId}/occurrences/{occurrence_id}"
   * @param updated an Occurrence object representing the desired updates to push
   * @throws Exception on errors while closing the client
   */
  public static void updateOccurrence(String occurrenceName, Occurrence updated) throws Exception {
    try (ContainerAnalysisClient client = ContainerAnalysisClient.create()) {
      client.updateOccurrence(occurrenceName, updated);
    }
  }
  // [END update_occurrence]

  // [START delete_note]
  /**
   * Deletes an existing note
   *
   * @param noteId the identifier of the note to delete
   * @param projectId the GCP project the occurrence will be created under
   * @throws Exception on errors while closing the client
   */
  public static void deleteNote(String noteId, String projectId) throws Exception {
    try (ContainerAnalysisClient client = ContainerAnalysisClient.create()) {
      final String noteName = client.formatNoteName(projectId, noteId);

      client.deleteNote(noteName);
    }
  }
  // [END delete_note]

  // [START delete_occurrence]
  /**
   * Deletes an existing occurrence
   *
   * @param occurrenceName the name of the occurrence to delete
   *                       format: "projects/{projectId}/occurrences/{occurrence_id}"
   * @throws Exception on errors while closing the client
   */
  public static void deleteOccurrence(String occurrenceName) throws Exception {
    try (ContainerAnalysisClient client = ContainerAnalysisClient.create()) {
      client.deleteOccurrence(occurrenceName);
    }
  }
  // [END delete_occurrence]


  // [START get_occurrence]
  /**
   * Retrieves an occurrence based on it's name
   *
   * @param occurrenceName the name of the occurrence to delete
   *                       format: "projects/{projectId}/occurrences/{occurrence_id}"
   * @return the specified Occurrence object
   * @throws Exception on errors while closing the client
   */
  public static Occurrence getOccurrence(String occurrenceName) throws  Exception {
    try (ContainerAnalysisClient client = ContainerAnalysisClient.create()) {
      return client.getOccurrence(occurrenceName);
    }
  }
  // [END get_occurrence]

  // [START get_note]
  /**
   * Retrieves a note based on it's noteId and projectId
   *
   * @param noteId the note's unique identifier
   * @param projectId the project's unique identifier
   * @return the specified Note object
   * @throws Exception on errors while closing the client
   */
  public static Note getNote(String noteId, String projectId) throws  Exception {
    try (ContainerAnalysisClient client = ContainerAnalysisClient.create()) {
      final String noteName = client.formatNoteName(projectId, noteId);

      return client.getNote(noteName);
    }
  }
  // [END get_note]

  // [START discovery_info]
  /**
   * Retrieves the Discovery occurrence created for a specified image
   * This occurrence contains information about the initial scan on the image
   *
   * @param imageUrl the Container Registry URL associated with the image
   *                 example: "https://gcr.io/project/image@sha256:foo"
   * @param projectId the GCP project the occurrence will be created under
   * @throws Exception on errors while closing the client
   */
  public static void getDiscoveryInfo(String imageUrl, String projectId) throws Exception {
    String filterStr = "kind=\"DISCOVERY\" AND resourceUrl=\"" + imageUrl + "\"";
    try (ContainerAnalysisClient client = ContainerAnalysisClient.create()) {
      final String projectName = client.formatProjectName(projectId);
      ListOccurrencesRequest.Builder req = ListOccurrencesRequest.newBuilder();
      req.setFilter(filterStr).setParent(projectName);
      ListOccurrencesPagedResponse response = client.listOccurrences(req.build());

      for (Occurrence o : response.iterateAll()) {
        System.out.println(o);
      }
    }
  }
  // [END discovery_info]

  // [START occurrences_for_note]
  /**
   * Retrieves all the occurrences associated with a specified note
   *
   * @param noteId the note's unique identifier
   * @param projectId the project's unique identifier
   * @return number of occurrences found
   * @throws Exception on errors while closing the client
   */
  public static int getOccurrencesForNote(String noteId, String projectId) throws Exception {
    try (ContainerAnalysisClient client = ContainerAnalysisClient.create()) {
      final String parentNoteName = client.formatNoteName(projectId, noteId);
      int i = 0;

      ListNoteOccurrencesPagedResponse response = client.listNoteOccurrences(parentNoteName);
      for (Occurrence o : response.iterateAll()) {
        //print and count each occurrence found
        System.out.println(o.getName());
        i = i + 1;
      }
      return i;
    }
  }
  // [END occurrences_for_note]


  // [START occurrences_for_image]
  /**
   * Retrieves all the occurrences associated with a specified image
   *
   * @param imageUrl the Container Registry URL associated with the image
   *                 example: "https://gcr.io/project/image@sha256:foo"
   * @param projectId the project's unique identifier
   * @return number of occurrences found
   * @throws Exception on errors while closing the client
   */
  public static int getOccurrencesForImage(String imageUrl, String projectId) throws Exception {
    try (ContainerAnalysisClient client = ContainerAnalysisClient.create()) {
      final String filterStr = "resourceUrl=\"" + imageUrl + "\"";
      final String projectName = client.formatProjectName(projectId);
      int i = 0;

      //build the request
      ListOccurrencesRequest.Builder b = ListOccurrencesRequest.newBuilder();
      b.setFilter(filterStr);
      b.setParent(projectName);
      ListOccurrencesRequest req = b.build();
      //query for the requested occurrences
      ListOccurrencesPagedResponse response = client.listOccurrences(req);
      for (Occurrence o : response.iterateAll()) {
        //print and count each occurrence found
        System.out.println(o.getName());
        i = i + 1;
      }
      return i;
    }
  }
  // [END occurrences_for_image]

  // [START pubsub]
  /**
   * Handle incoming occurrences using a pubsub subscription
   * @param subscriptionId the user-specified identifier for the pubsub subscription
   * @param timeout the amount of time to listen for pubsub messages (in seconds)
   * @param projectId the project's unique identifier
   * @return number of occurrence pubsub messages received
   * @throws Exception on errors with the subscription client
   */
  public static int pubSub(String subscriptionId, int timeout, String projectId) throws Exception {
    Subscriber subscriber = null;
    MessageReceiverExample receiver = new MessageReceiverExample();

    try {
      // subscribe to the requested pubsub channel
      ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(
          projectId, subscriptionId);
      subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
      subscriber.startAsync().awaitRunning();
      // listen to messages for 'listenTimeout' seconds
      for (int i = 0; i < timeout; i++) {
        sleep(1000);
      }
    } finally {
      // stop listening to the channel
      if (subscriber != null) {
        subscriber.stopAsync();
      }
    }
    //print and return the number of pubsub messages received
    System.out.println(receiver.messageCount);
    return receiver.messageCount;
  }


  /**
   * Custom class to handle incoming pubsub messages
   * In this case, the class will simply log and count each message as it comes in
   */
  static class MessageReceiverExample implements MessageReceiver {
    public int messageCount = 0;

    @Override
    public synchronized void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
      //every time a pubsub message comes in, print it and count it
      System.out.println("Message " + messageCount + ": " + message.getData().toStringUtf8());
      messageCount += 1;
      //acknowledge the message
      consumer.ack();
    }
  }

  /**
   * Creates and returns a pubsub subscription object listening to the occurrence topic
   * @param subscriptionId the identifier you want to associate with the subscription
   * @param projectId the project's unique identifier
   * @throws Exception on errors with the subscription client
   */
  public static Subscription createOccurrenceSubscription(String subscriptionId, String projectId)
      throws Exception {
    String topicId = "resource-notes-occurrences-v1alpha1";
    try (SubscriptionAdminClient client = SubscriptionAdminClient.create()) {
      PushConfig config = PushConfig.getDefaultInstance();
      ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
      ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(
          projectId, subscriptionId);
      Subscription sub = client.createSubscription(
          subscriptionName, topicName, config, 0);
      return sub;
    }
  }
  // [END pubsub]
}