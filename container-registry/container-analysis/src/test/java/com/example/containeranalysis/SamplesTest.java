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
import static junit.framework.TestCase.assertEquals;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.devtools.containeranalysis.v1beta1.GrafeasV1Beta1Client;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.containeranalysis.v1beta1.NoteName;
import com.google.containeranalysis.v1beta1.ProjectName;
import com.google.pubsub.v1.ProjectSubscriptionName;
import io.grafeas.v1beta1.Note;
import io.grafeas.v1beta1.Occurrence;
import io.grafeas.v1beta1.Resource;
import io.grafeas.v1beta1.discovery.Discovered;
import io.grafeas.v1beta1.discovery.Discovered.AnalysisStatus;
import io.grafeas.v1beta1.discovery.Discovery;
import io.grafeas.v1beta1.vulnerability.Details;
import io.grafeas.v1beta1.vulnerability.Severity;
import io.grafeas.v1beta1.vulnerability.Vulnerability;
import io.grpc.StatusRuntimeException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * test runner
 */
@RunWith(JUnit4.class)
@SuppressWarnings("checkstyle:abbreviationaswordinname")
public class SamplesTest {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String subId = "CA-Occurrences-" + (new Date()).getTime();
  private String noteId;
  private String imageUrl;
  private Note noteObj;
  private static final int SLEEP_TIME = 1000;
  private static final int TRY_LIMIT = 10;

  @Rule
  public TestName name = new TestName();



  @AfterClass
  public static void tearDownClass() {
    try {
      SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create();
      ProjectSubscriptionName subName = ProjectSubscriptionName.of(PROJECT_ID, subId);
      subscriptionAdminClient.deleteSubscription(subName);
      subscriptionAdminClient.shutdownNow();
    } catch (Exception e) {
      // these exceptions aren't relevant to the tests
      System.out.println("TearDownClass Error: " + e.toString());
    }
  }


  @Before
  public void setUp() throws Exception {
    System.out.println(name.getMethodName());
    noteId =  "note-" + (new Date()).getTime() + name.getMethodName();
    imageUrl = "www." + (new Date()).getTime() + name.getMethodName() + ".com";
    noteObj = CreateNote.createNote(noteId, PROJECT_ID);
  }

  @After
  public void tearDown() {
    try {
      DeleteNote.deleteNote(noteId, PROJECT_ID);
    } catch (Exception e) {
      // these exceptions aren't relevant to the tests
      System.out.println("TearDown Error: " + e.toString());
    }
  }

  @Test
  public void testCreateNote() throws Exception {
    // note should have been created as part of set up. verify that it succeeded
    Note n = GetNote.getNote(noteId, PROJECT_ID);

    assertEquals(n.getName(), noteObj.getName());
  }

  @Test
  public void testDeleteNote() throws Exception {
    DeleteNote.deleteNote(noteId, PROJECT_ID);
    try {
      GetNote.getNote(noteId, PROJECT_ID);
      // above should throw, because note was deleted
      Assert.fail("note not deleted");
    } catch (NotFoundException e) {
      // test passes
    }
  }

  @Test
  public void testCreateOccurrence() throws Exception {
    Occurrence o = CreateOccurrence.createOccurrence(imageUrl, noteId, PROJECT_ID, PROJECT_ID);
    String[] nameArr = o.getName().split("/");
    String occId = nameArr[nameArr.length - 1];
    Occurrence retrieved = GetOccurrence.getOccurrence(occId, PROJECT_ID);
    assertEquals(o.getName(), retrieved.getName());

    // clean up
    DeleteOccurrence.deleteOccurrence(occId, PROJECT_ID);
  }

  @Test
  public void testDeleteOccurrence() throws Exception {
    Occurrence o = CreateOccurrence.createOccurrence(imageUrl, noteId, PROJECT_ID, PROJECT_ID);
    String occName = o.getName();
    String[] nameArr = occName.split("/");
    String occId = nameArr[nameArr.length - 1];

    DeleteOccurrence.deleteOccurrence(occId, PROJECT_ID);

    try {
      GetOccurrence.getOccurrence(occId, PROJECT_ID);
      // getOccurrence should fail, because occurrence was deleted
      Assert.fail("failed to delete occurrence");
    } catch (NotFoundException e) {
      // test passes
    }
  }

  @Test
  public void testOccurrencesForImage() throws Exception {
    int newCount;
    int tries = 0;
    int origCount = OccurrencesForImage.getOccurrencesForImage(imageUrl, PROJECT_ID);
    final Occurrence o = CreateOccurrence.createOccurrence(
        imageUrl, noteId, PROJECT_ID, PROJECT_ID);
    do {
      newCount = OccurrencesForImage.getOccurrencesForImage(imageUrl, PROJECT_ID);
      sleep(SLEEP_TIME);
      tries += 1;
    } while (newCount != 1 && tries < TRY_LIMIT);
    assertEquals(1, newCount);
    assertEquals(0, origCount);

    // clean up
    String[] nameArr = o.getName().split("/");
    String occId = nameArr[nameArr.length - 1];
    DeleteOccurrence.deleteOccurrence(occId, PROJECT_ID);
  }

  @Test
  public void testOccurrencesForNote() throws Exception {
    int newCount;
    int tries = 0;
    int origCount = OccurrencesForNote.getOccurrencesForNote(noteId, PROJECT_ID);
    final Occurrence o = CreateOccurrence.createOccurrence(
        imageUrl, noteId, PROJECT_ID, PROJECT_ID);
    do {
      newCount = OccurrencesForNote.getOccurrencesForNote(noteId, PROJECT_ID);
      sleep(SLEEP_TIME);
      tries += 1;
    } while (newCount != 1 && tries < TRY_LIMIT);
    assertEquals(0, origCount);
    assertEquals(1, newCount);

    // clean up
    String[] nameArr = o.getName().split("/");
    String occId = nameArr[nameArr.length - 1];
    DeleteOccurrence.deleteOccurrence(occId, PROJECT_ID);
  }

  @Test
  public void testPubSub() throws Exception {
    int newCount;
    int tries;
    ProjectSubscriptionName subName = ProjectSubscriptionName.of(PROJECT_ID, subId);
    try {
      Subscriptions.createOccurrenceSubscription(subId, PROJECT_ID);
    } catch (StatusRuntimeException e) {
      System.out.println("subscription " + subId + " already exists");
    }
    Subscriber subscriber = null;
    Subscriptions.MessageReceiverExample receiver = new Subscriptions.MessageReceiverExample();

    subscriber = Subscriber.newBuilder(subName, receiver).build();
    subscriber.startAsync().awaitRunning();
    // sleep so any messages in the queue can go through and be counted before we start the test
    sleep(SLEEP_TIME * 3);
    // set the initial state of our counter
    int startVal = receiver.messageCount + 1;
    // now, we can test adding 3 more occurrences
    int endVal = startVal + 3;
    for (int i = startVal; i <= endVal; i++) {
      Occurrence o = CreateOccurrence.createOccurrence(imageUrl, noteId, PROJECT_ID, PROJECT_ID);
      System.out.println("CREATED: " + o.getName());
      tries = 0;
      do {
        newCount = receiver.messageCount;
        sleep(SLEEP_TIME);
        tries += 1;
      } while (newCount != i && tries < TRY_LIMIT);
      System.out.println(receiver.messageCount + " : " + i);
      assertEquals(i, receiver.messageCount);
      String[] nameArr = o.getName().split("/");
      String occId = nameArr[nameArr.length - 1];
      DeleteOccurrence.deleteOccurrence(occId, PROJECT_ID);
    }
    if (subscriber != null) {
      subscriber.stopAsync();
    }
  }

  @Test 
  public void testPollDiscoveryOccurrenceFinished() throws Exception {
    try {
      // expect fail on first try
      PollDiscoveryOccurrenceFinished.pollDiscoveryOccurrenceFinished(imageUrl, PROJECT_ID, 5);
      Assert.fail("found unexpected discovery occurrence");
    } catch (TimeoutException e) {
      // test passes
    }
    // create discovery note
    String discNoteId = "discovery-note-" + (new Date()).getTime();
    NoteName noteName = NoteName.of(PROJECT_ID, discNoteId);
    Note.Builder noteBuilder = Note.newBuilder();
    Discovery.Builder discoveryBuilder = Discovery.newBuilder();
    noteBuilder.setDiscovery(discoveryBuilder);
    Note newNote = noteBuilder.build();
    GrafeasV1Beta1Client client = GrafeasV1Beta1Client.create();
    client.createNote(ProjectName.format(PROJECT_ID), discNoteId, newNote);

    // create discovery occurrence
    Occurrence.Builder occBuilder = Occurrence.newBuilder();
    occBuilder.setNoteName(noteName.toString());
    Discovered.Builder discoveredBuilder = Discovered.newBuilder();
    discoveredBuilder.setAnalysisStatus(AnalysisStatus.FINISHED_SUCCESS);
    io.grafeas.v1beta1.discovery.Details.Builder detailsBuilder = 
        io.grafeas.v1beta1.discovery.Details.newBuilder();
    detailsBuilder.setDiscovered(discoveredBuilder);
    occBuilder.setDiscovered(detailsBuilder);
    Resource.Builder resourceBuilder = Resource.newBuilder();
    resourceBuilder.setUri(imageUrl);
    occBuilder.setResource(resourceBuilder);
    Occurrence newOcc = occBuilder.build();
    Occurrence result = client.createOccurrence(ProjectName.format(PROJECT_ID), newOcc);

    // poll again
    Occurrence found = PollDiscoveryOccurrenceFinished.pollDiscoveryOccurrenceFinished(
        imageUrl, PROJECT_ID, 5);
    AnalysisStatus foundStatus = found.getDiscovered().getDiscovered().getAnalysisStatus();
    assertEquals(foundStatus, AnalysisStatus.FINISHED_SUCCESS);

    // clean up
    String[] nameArr = found.getName().split("/");
    String occId = nameArr[nameArr.length - 1];
    DeleteOccurrence.deleteOccurrence(occId, PROJECT_ID);
    DeleteNote.deleteNote(discNoteId, PROJECT_ID);
  }

  @Test 
  public void testFindVulnerabilitiesForImage() throws Exception {
    List<Occurrence> result = VulnerabilityOccurrencesForImage.findVulnerabilityOccurrencesForImage(
        imageUrl, PROJECT_ID);
    assertEquals(result.size(), 0);
    Occurrence o = CreateOccurrence.createOccurrence(imageUrl, noteId, PROJECT_ID, PROJECT_ID);
    int tries = 0;
    do {
      result = VulnerabilityOccurrencesForImage.findVulnerabilityOccurrencesForImage(
          imageUrl, PROJECT_ID);
      sleep(SLEEP_TIME);
      tries += 1;
    } while (result.size() != 1 && tries < TRY_LIMIT);
    assertEquals(result.size(), 1);

    // clean up
    String[] nameArr = o.getName().split("/");
    String occId = nameArr[nameArr.length - 1];
    DeleteOccurrence.deleteOccurrence(occId, PROJECT_ID);
  }

  @Test 
  public void testFindHighSeverityVulnerabilitiesForImage() throws Exception {
    // check before creation
    List<Occurrence> result = HighVulnerabilitiesForImage.findHighSeverityVulnerabilitiesForImage(
        imageUrl, PROJECT_ID);
    assertEquals(0, result.size());

    // create low severity occurrence
    Occurrence low;
    low = CreateOccurrence.createOccurrence(imageUrl, noteId, PROJECT_ID, PROJECT_ID);
    result = HighVulnerabilitiesForImage.findHighSeverityVulnerabilitiesForImage(
        imageUrl, PROJECT_ID);
    assertEquals(0, result.size());

    // create high severity note
    String vulnNoteId = "discovery-note-" + (new Date()).getTime();
    Note.Builder noteBuilder = Note.newBuilder();
    Vulnerability.Builder vulnBuilder = Vulnerability.newBuilder();
    vulnBuilder.setSeverity(Severity.CRITICAL);
    noteBuilder.setVulnerability(vulnBuilder);
    Note newNote = noteBuilder.build();
    GrafeasV1Beta1Client client = GrafeasV1Beta1Client.create();
    client.createNote(ProjectName.format(PROJECT_ID), vulnNoteId, newNote);

    // create high severity occurrence
    Occurrence.Builder occBuilder = Occurrence.newBuilder();
    NoteName noteName = NoteName.of(PROJECT_ID, vulnNoteId);
    occBuilder.setNoteName(noteName.toString());
    Details.Builder detailsBuilder = Details.newBuilder();
    occBuilder.setVulnerability(detailsBuilder);
    Resource.Builder resourceBuilder = Resource.newBuilder();
    resourceBuilder.setUri(imageUrl);
    occBuilder.setResource(resourceBuilder);
    Occurrence critical = occBuilder.build();
    critical = client.createOccurrence(ProjectName.format(PROJECT_ID), critical);

    // check again
    int tries = 0;
    do {
      result = HighVulnerabilitiesForImage.findHighSeverityVulnerabilitiesForImage(
          imageUrl, PROJECT_ID);
      sleep(SLEEP_TIME);
      tries += 1;
    } while (result.size() != 1 && tries < TRY_LIMIT);
    assertEquals(1, result.size());

    // clean up
    String[] lowNameArr = low.getName().split("/");
    String lowId = lowNameArr[lowNameArr.length - 1];
    DeleteOccurrence.deleteOccurrence(lowId, PROJECT_ID);
    String[] nameArr = critical.getName().split("/");
    String occId = nameArr[nameArr.length - 1];
    DeleteOccurrence.deleteOccurrence(occId, PROJECT_ID);
    DeleteNote.deleteNote(vulnNoteId, PROJECT_ID);
  }
}
