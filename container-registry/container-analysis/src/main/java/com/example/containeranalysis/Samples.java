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

package com.example.containeranalysis;

import static java.lang.Thread.sleep;

import com.google.cloud.devtools.containeranalysis.v1beta1.ContainerAnalysisV1Beta1Client;
import com.google.cloud.devtools.containeranalysis.v1beta1.GrafeasV1Beta1Client;
import com.google.cloud.devtools.containeranalysis.v1beta1.GrafeasV1Beta1Client.ListNoteOccurrencesPagedResponse;
import com.google.cloud.devtools.containeranalysis.v1beta1.GrafeasV1Beta1Client.ListOccurrencesPagedResponse;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.containeranalysis.v1beta1.NoteName;
import com.google.containeranalysis.v1beta1.ProjectName;
import com.google.pubsub.v1.ProjectSubscriptionName;
import com.google.pubsub.v1.ProjectTopicName;
import com.google.pubsub.v1.PubsubMessage;
import com.google.pubsub.v1.PushConfig;
import com.google.pubsub.v1.Subscription;
import io.grafeas.v1beta1.ListNoteOccurrencesRequest;
import io.grafeas.v1beta1.ListOccurrencesRequest;
import io.grafeas.v1beta1.Note;
import io.grafeas.v1beta1.Occurrence;
import io.grafeas.v1beta1.Resource;
import io.grafeas.v1beta1.UpdateNoteRequest;
import io.grafeas.v1beta1.UpdateOccurrenceRequest;
import io.grafeas.v1beta1.vulnerability.Details;
import io.grafeas.v1beta1.vulnerability.Severity;
import io.grafeas.v1beta1.vulnerability.Vulnerability;
import io.grpc.StatusRuntimeException;
import java.io.IOException;
import java.lang.InterruptedException;

/**
 * API usage samples
 */
public class Samples {

  // [START create_note]
  /**
   * Creates and returns a new vulnerability Note
   * @param client The Grafeas client used to perform the API requests.
   * @param noteId A user-specified identifier for the Note.
   * @param projectId the GCP project the Note will be created under
   * @return the newly created Note object
   */
  public static Note createNote(GrafeasV1Beta1Client client, String noteId, String projectId) {
    Note.Builder noteBuilder = Note.newBuilder();
    Vulnerability.Builder vulBuilder = Vulnerability.newBuilder();
    // Details about the your vulnerability can be added here
    // Example: vulBuilder.setSeverity(Severity.CRITICAL);
    noteBuilder.setVulnerability(vulBuilder);
    Note newNote = noteBuilder.build();

    final String projectName = ProjectName.format(projectId);
    return client.createNote(projectName, noteId, newNote);
  }
  // [END create_note]


  // [START create_occurrence]
  /**
   * Creates and returns a new Occurrence of a previously created vulnerability Note
   * @param client The Grafeas client used to perform the API requests.
   * @param imageUrl the Container Registry URL associated with the image
   *                 example: "https://gcr.io/project/image@sha256:foo"
   * @param noteId the identifier of the Note associated with this Occurrence
   * @param occProjectId the GCP project the Occurrence will be created under
   * @param noteProjectId the GCP project the associated Note belongs to
   * @return the newly created Occurrence object
   */
  public static Occurrence createOccurrence(GrafeasV1Beta1Client client, String imageUrl, 
      String noteId, String occProjectId, String noteProjectId) {
    final NoteName noteName = NoteName.of(noteProjectId, noteId);
    final String occProjectName = ProjectName.format(occProjectId);

    Occurrence.Builder occBuilder = Occurrence.newBuilder();
    occBuilder.setNoteName(noteName.toString());
    Details.Builder detailsBuilder = Details.newBuilder();
    // Details about the vulnerability instance can be added here
    occBuilder.setVulnerability(detailsBuilder);
    // Attach the occurrence to the associated image uri
    Resource.Builder resourceBuilder = Resource.newBuilder();
    resourceBuilder.setUri(imageUrl);
    occBuilder.setResource(resourceBuilder);
    Occurrence newOcc = occBuilder.build();
    return client.createOccurrence(occProjectName, newOcc);
  }
  // [END create_occurrence]

  // [START update_note]
  /**
   * Pushes an update to a Note that already exists on the server
   * @param client The Grafeas client used to perform the API requests.
   * @param updated a Note object representing the desired updates to push
   * @param noteId the identifier of the existing Note
   * @param projectId the GCP project the Note belongs to
   */
  public static Note updateNote(GrafeasV1Beta1Client client, Note updated, String noteId, 
      String projectId) {
    final NoteName noteName = NoteName.of(projectId, noteId);
    UpdateNoteRequest request = UpdateNoteRequest.newBuilder()
                                                 .setName(noteName.toString())
                                                 .setNote(updated)
                                                 .build();
    return client.updateNote(request);
  }
  // [END update_note]

  // [START update_occurrence]
  /**
   * Pushes an update to an Occurrence that already exists on the server
   * @param client The Grafeas client used to perform the API requests.
   * @param occurrenceName the name of the Occurrence to delete.
   *                       format: "projects/[PROJECT_ID]/occurrences/[OCCURRENCE_ID]"
   * @param updated an Occurrence object representing the desired updates to push
   */
  public static Occurrence updateOccurrence(GrafeasV1Beta1Client client, String occurrenceName,
      Occurrence updated) {
    UpdateOccurrenceRequest request = UpdateOccurrenceRequest.newBuilder()
                                                             .setName(occurrenceName)
                                                             .setOccurrence(updated)
                                                             .build();
    return client.updateOccurrence(request);
  }
  // [END update_occurrence]

  // [START delete_note]
  /**
   * Deletes an existing Note from the server
   * @param client The Grafeas client used to perform the API requests.
   * @param noteId the identifier of the Note to delete
   * @param projectId the GCP project the Note belongs to
   */
  public static void deleteNote(GrafeasV1Beta1Client client, String noteId, String projectId) {
    final NoteName noteName = NoteName.of(projectId, noteId);
    client.deleteNote(noteName);
  }
  // [END delete_note]

  // [START delete_occurrence]
  /**
   * Deletes an existing Occurrence from the server
   * @param client The Grafeas client used to perform the API requests.
   * @param occurrenceName the name of the Occurrence to delete
   *                       format: "projects/[PROJECT_ID]/occurrences/[OCCURRENCE_ID]"
   */
  public static void deleteOccurrence(GrafeasV1Beta1Client client, String occurrenceName) {
    client.deleteOccurrence(occurrenceName);
  }
  // [END delete_occurrence]


  // [START get_occurrence]
  /**
   * Retrieves and prints a specified Occurrence from the server
   * @param client The Grafeas client used to perform the API requests.
   * @param occurrenceName the name of the Occurrence to delete
   *                       format: "projects/[PROJECT_ID]/occurrences/[OCCURRENCE_ID]"
   * @return the requested Occurrence object
   */
  public static Occurrence getOccurrence(GrafeasV1Beta1Client client, String occurrenceName) {
    Occurrence occ = client.getOccurrence(occurrenceName);
    System.out.println(occ);
    return occ;
  }
  // [END get_occurrence]

  // [START get_note]
  /**
   * Retrieves and prints a specified Note from the server
   * @param client The Grafeas client used to perform the API requests.
   * @param noteId the Note's unique identifier
   * @param projectId the GCP project the Note belongs to
   * @return the requested Note object
   */
  public static Note getNote(GrafeasV1Beta1Client client, String noteId, String projectId) {
    final NoteName noteName = NoteName.of(projectId, noteId);

    Note n = client.getNote(noteName);
    System.out.println(n);
    return n;
  }
  // [END get_note]

  // [START discovery_info]
  /**
   * Retrieves and prints the Discovery Occurrence created for a specified image
   * The Discovery Occurrence contains information about the initial scan on the image
   * @param client The Grafeas client used to perform the API requests.
   * @param imageUrl the Container Registry URL associated with the image
   *                 example: "https://gcr.io/project/image@sha256:foo"
   * @param projectId the GCP project the image belongs to
   */
  public static void getDiscoveryInfo(GrafeasV1Beta1Client client, String imageUrl,
      String projectId) {
    String filterStr = "kind=\"DISCOVERY\" AND resourceUrl=\"" + imageUrl + "\"";
    final String projectName = ProjectName.format(projectId);

    for (Occurrence o : client.listOccurrences(projectName, filterStr).iterateAll()) {
      System.out.println(o);
    }
  }
  // [END discovery_info]

  // [START occurrences_for_note]
  /**
   * Retrieves all the Occurrences associated with a specified Note
   * Here, all Occurrences are printed and counted
   * @param client The Grafeas client used to perform the API requests.
   * @param noteId the Note's unique identifier
   * @param projectId the GCP project the Note belongs to
   * @return number of Occurrences found
   */
  public static int getOccurrencesForNote(GrafeasV1Beta1Client client, String noteId, 
      String projectId) {
    final NoteName noteName = NoteName.of(projectId, noteId);
    int i = 0;

    ListNoteOccurrencesRequest request = ListNoteOccurrencesRequest.newBuilder()
                                                                   .setName(noteName.toString())
                                                                   .build();
    for (Occurrence o : client.listNoteOccurrences(request).iterateAll()) {
      // Write custom code to process each Occurrence here
      System.out.println(o.getName());
      i = i + 1;
    }
    return i;
  }
  // [END occurrences_for_note]


  // [START occurrences_for_image]
  /**
   * Retrieves all the Occurrences associated with a specified image
   * Here, all Occurrences are simply printed and counted
   * @param client The Grafeas client used to perform the API requests.
   * @param imageUrl the Container Registry URL associated with the image
   *                 example: "https://gcr.io/project/image@sha256:foo"
   * @param projectId the GCP project to search for Occurrences in
   * @return number of Occurrences found
   */
  public static int getOccurrencesForImage(GrafeasV1Beta1Client client, String imageUrl, 
      String projectId) {
    final String filterStr = "resourceUrl=\"" + imageUrl + "\"";
    final String projectName = ProjectName.format(projectId);
    int i = 0;

    for (Occurrence o : client.listOccurrences(projectName, filterStr).iterateAll()) {
      // Write custom code to process each Occurrence here
      System.out.println(o.getName());
      i = i + 1;
    }
    return i;
  }
  // [END occurrences_for_image]

  // [START pubsub]
  /**
   * Handle incoming Occurrences using a Cloud Pub/Sub subscription
   * @param subId the user-specified identifier for the Pub/Sub subscription
   * @param timeout the amount of time to listen for Pub/Sub messages (in seconds)
   * @param projectId the GCP project the Pub/Sub subscription belongs to
   * @return number of Occurrence Pub/Sub messages received before exiting
   * @throws InterruptedException on errors with the subscription client
   */
  public static int pubSub(String subId, int timeout, String projectId) 
      throws InterruptedException {
    Subscriber subscriber = null;
    MessageReceiverExample receiver = new MessageReceiverExample();

    try {
      // Subscribe to the requested Pub/Sub channel
      ProjectSubscriptionName subName = ProjectSubscriptionName.of(projectId, subId);
      subscriber = Subscriber.newBuilder(subName, receiver).build();
      subscriber.startAsync().awaitRunning();
      // Listen to messages for 'timeout' seconds
      for (int i = 0; i < timeout; i++) {
        sleep(1000);
      }
    } finally {
      // Stop listening to the channel
      if (subscriber != null) {
        subscriber.stopAsync();
      }
    }
    // Print and return the number of Pub/Sub messages received
    System.out.println(receiver.messageCount);
    return receiver.messageCount;
  }


  /**
   * Custom class to handle incoming Pub/Sub messages
   * In this case, the class will simply log and count each message as it comes in
   */
  static class MessageReceiverExample implements MessageReceiver {
    public int messageCount = 0;

    @Override
    public synchronized void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
      // Every time a Pub/Sub message comes in, print it and count it
      System.out.println("Message " + messageCount + ": " + message.getData().toStringUtf8());
      messageCount += 1;
      // Acknowledge the message
      consumer.ack();
    }
  }

  /**
   * Creates and returns a Pub/Sub subscription object listening to the Occurrence topic
   * @param subId the identifier you want to associate with the subscription
   * @param projectId the GCP project to create the subscription under
   * @throws IOException thrown on errors with the subscription client
   * @throws StatusRuntimeException if subscription already exists
   *
   */
  public static Subscription createOccurrenceSubscription(String subId, String projectId) 
      throws IOException, StatusRuntimeException {
    // This topic id will automatically receive messages when Occurrences are added or modified
    String topicId = "container-analysis-occurrences-v1beta1";
    SubscriptionAdminClient client = SubscriptionAdminClient.create();
    PushConfig config = PushConfig.getDefaultInstance();
    ProjectTopicName topicName = ProjectTopicName.of(projectId, topicId);
    ProjectSubscriptionName subName = ProjectSubscriptionName.of(projectId, subId);
    Subscription sub = client.createSubscription(subName, topicName, config, 0);
    return sub;
  } 
  // [END pubsub]
}
