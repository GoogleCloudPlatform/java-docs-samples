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
import static junit.framework.Assert.fail;
import static junit.framework.TestCase.assertEquals;

import com.google.cloud.pubsub.v1.Subscriber;
import com.google.cloud.pubsub.v1.SubscriptionAdminClient;
import com.google.containeranalysis.v1alpha1.Note;
import com.google.containeranalysis.v1alpha1.Occurrence;
import com.google.containeranalysis.v1alpha1.VulnerabilityType.VulnerabilityDetails;
import com.google.pubsub.v1.ProjectSubscriptionName;
import java.util.Date;
import org.junit.After;
import org.junit.Before;
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
public class SamplesTests {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private String noteId;
  private String imageUrl;
  private Note noteObj;
  private static final int SLEEP_TIME = 1000;
  private static final int TRY_LIMIT = 10;

  @Rule
  public TestName name = new TestName();

  @Before
  public void setUp() {
    System.out.println(name.getMethodName());
    noteId =  "note-" + (new Date()).getTime() + name.getMethodName();
    imageUrl = "www." + (new Date()).getTime() + name.getMethodName() + ".com";

    try {
      noteObj = Samples.createNote(noteId, PROJECT_ID);
    } catch (Exception e) {
      // note creation fails
    }
  }

  @After
  public void tearDown() {
    try {
      Samples.deleteNote(noteId, PROJECT_ID);
    } catch (Exception e) {
      // note deletion fails
    }
  }

  @Test
  public void testCreateNote() throws Exception {

    // note should have been created as part of set up. verify that it succeeded
    Note n = Samples.getNote(noteId, PROJECT_ID);

    assertEquals(n.getName(), noteObj.getName());

  }

  @Test
  public void testDeleteNote() throws Exception {
    Samples.deleteNote(noteId, PROJECT_ID);
    try {
      Samples.getNote(noteId, PROJECT_ID);
      //above should throw, because note was deleted
      fail("note not deleted");
    } catch (Exception e) {
      // test passes
    }

  }

  @Test
  public void testUpdateNote() throws Exception {
    String descriptionText = "updated";

    Note.Builder builder = Note.newBuilder(noteObj);
    builder.setShortDescription(descriptionText);
    Samples.updateNote(builder.build(), noteId, PROJECT_ID);

    Note updated = Samples.getNote(noteId, PROJECT_ID);
    assertEquals(descriptionText, updated.getShortDescription());
  }

  @Test
  public void testCreateOccurrence() throws Exception {

    Occurrence o = Samples.createOccurrence(imageUrl, noteId, PROJECT_ID);
    Occurrence retrieved = Samples.getOccurrence(o.getName());
    assertEquals(o.getName(), retrieved.getName());

    //clean up
    Samples.deleteOccurrence(o.getName());
  }

  @Test
  public void testDeleteOccurrence() throws Exception {

    Occurrence o = Samples.createOccurrence(imageUrl, noteId, PROJECT_ID);
    String occName = o.getName();

    Samples.deleteOccurrence(occName);

    try {
      Samples.getOccurrence(occName);
      //getOccurrence should fail, because occurrence was deleted
      fail("failed to delete occurrence");
    } catch (Exception e) {
      //test passes
    }
  }

  @Test
  public void testUpdateOccurrence() throws Exception {
    String typeId = "newType";

    Occurrence o = Samples.createOccurrence(imageUrl, noteId, PROJECT_ID);

    Occurrence.Builder b = Occurrence.newBuilder(o);
    VulnerabilityDetails.Builder v = VulnerabilityDetails.newBuilder();
    v.setType(typeId);
    b.setVulnerabilityDetails(v.build());
    Samples.updateOccurrence(o.getName(), b.build());
    Occurrence o2 = Samples.getOccurrence(o.getName());
    assertEquals(typeId, o2.getVulnerabilityDetails().getType());

    //clean up
    Samples.deleteOccurrence(o2.getName());
  }

  @Test
  public void testOccurrencesForImage() throws Exception {
    int newCount;
    int tries = 0;
    int origCount = Samples.getOccurrencesForImage(imageUrl, PROJECT_ID);
    final Occurrence o = Samples.createOccurrence(imageUrl, noteId, PROJECT_ID);
    do {
      newCount = Samples.getOccurrencesForImage(imageUrl, PROJECT_ID);
      sleep(SLEEP_TIME);
    } while (newCount != 1 && tries < TRY_LIMIT);
    assertEquals(1, newCount);
    assertEquals(0, origCount);

    //clean up
    Samples.deleteOccurrence(o.getName());
  }

  @Test
  public void testOccurrencesForNote() throws Exception {
    int newCount;
    int tries = 0;
    int origCount = Samples.getOccurrencesForNote(noteId, PROJECT_ID);
    final Occurrence o = Samples.createOccurrence(imageUrl, noteId, PROJECT_ID);
    do {
      newCount = Samples.getOccurrencesForNote(noteId, PROJECT_ID);
      sleep(SLEEP_TIME);
    } while (newCount != 1 && tries < TRY_LIMIT);
    assertEquals(0, origCount);
    assertEquals(1, newCount);

    //clean up
    Samples.deleteOccurrence(o.getName());
  }

  @Test
  public void testPubSub() throws Exception {
    int newCount;
    int tries;
    String subscriptionId = "drydockOccurrences";
    ProjectSubscriptionName subscriptionName = ProjectSubscriptionName.of(PROJECT_ID, subscriptionId);

    Samples.createOccurrenceSubscription(subscriptionId, PROJECT_ID);
    Subscriber subscriber = null;
    Samples.MessageReceiverExample receiver = new Samples.MessageReceiverExample();
    try {
      subscriber = Subscriber.newBuilder(subscriptionName, receiver).build();
      subscriber.startAsync().awaitRunning();
      // sleep so any messages in the queue can go through and be counted before we start the test
      sleep(SLEEP_TIME);
      // set the initial state of our counter
      int startVal = receiver.messageCount + 1;
      // now, we can test adding 3 more occurrences
      int endVal = startVal + 3;
      for (int i = startVal; i <= endVal; i++) {
        Occurrence o = Samples.createOccurrence(imageUrl, noteId, PROJECT_ID);
        System.out.println("CREATED: " + o.getName());
        tries = 0;
        do {
          newCount = receiver.messageCount;
          sleep(SLEEP_TIME);
          tries += 1;
        } while (newCount != i && tries < TRY_LIMIT);
        System.out.println(receiver.messageCount + " : " + i);
        assertEquals(i, receiver.messageCount);
        Samples.deleteOccurrence(o.getName());
      }
    } catch (Exception e) {
      fail("exception thrown");
    } finally {
      if (subscriber != null) {
        subscriber.stopAsync();
      }
      //delete subscription now that we're done with it
      try (SubscriptionAdminClient subscriptionAdminClient = SubscriptionAdminClient.create()) {
        subscriptionAdminClient.deleteSubscription(subscriptionName);
      }
    }

  }
}
