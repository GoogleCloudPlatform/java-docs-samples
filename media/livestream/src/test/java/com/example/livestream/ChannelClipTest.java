/*
 * Copyright 2024 Google LLC
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

package com.example.livestream;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import com.google.api.gax.paging.Page;
import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.BucketInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageClass;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import com.google.cloud.video.livestream.v1.Clip;
import com.google.cloud.video.livestream.v1.Input;
import com.google.cloud.video.livestream.v1.LivestreamServiceClient.ListClipsPagedResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class ChannelClipTest {

  @Rule public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(5);

  private static final String LOCATION = "us-central1";
  private static final String BUCKET_NAME =
      "java-samples-livestream-test-" + UUID.randomUUID().toString().substring(0, 25);
  private static String PROJECT_ID;
  private static final String CHANNEL_ID =
      "my-channel-" + UUID.randomUUID().toString().substring(0, 25);
  private static final String INPUT_ID =
      "my-input-" + UUID.randomUUID().toString().substring(0, 25);
  private static final String CLIP_ID = "my-clip-" + UUID.randomUUID().toString().substring(0, 25);
  private static String CLIP_NAME;
  private static final String OUTPUT_URI = "gs://" + BUCKET_NAME + "/channel-test/";
  private static final String CLIP_OUTPUT_URI = OUTPUT_URI + "clips";

  private static PrintStream originalOut;
  private static ByteArrayOutputStream bout;

  private static String rtmpUri;

  private static String requireEnvVar(String varName) {
    String varValue = System.getenv(varName);
    assertNotNull(
        String.format("Environment variable '%s' is required to perform these tests.", varName));
    return varValue;
  }

  private static void deleteBucket() {
    Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    Bucket bucket = storage.get(ChannelClipTest.BUCKET_NAME);
    if (bucket != null) {
      Page<Blob> blobs = bucket.list();

      for (Blob blob : blobs.iterateAll()) {
        System.out.println(blob.getName());
        storage.delete(ChannelClipTest.BUCKET_NAME, blob.getName());
      }
      bucket.delete();
    }
  }

  @BeforeClass
  public static void beforeTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {

    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    PROJECT_ID = requireEnvVar("GOOGLE_CLOUD_PROJECT");

    originalOut = System.out;
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));

    // Clean up old resources in the test project.
    TestUtils.cleanAllStale(PROJECT_ID, LOCATION);
    deleteBucket();
    // Create temp bucket
    Storage storage = StorageOptions.newBuilder().setProjectId(PROJECT_ID).build().getService();
    storage.create(
        BucketInfo.newBuilder(BUCKET_NAME)
            .setStorageClass(StorageClass.STANDARD)
            .setLocation(LOCATION)
            .build());

    CLIP_NAME =
        String.format(
            "projects/%s/locations/%s/channels/%s/clips/%s",
            PROJECT_ID, LOCATION, CHANNEL_ID, CLIP_ID);

    Input input = CreateInput.createInput(PROJECT_ID, LOCATION, INPUT_ID);
    rtmpUri = input.getUri();
    CreateChannel.createChannel(PROJECT_ID, LOCATION, CHANNEL_ID, INPUT_ID, OUTPUT_URI);
    StartChannel.startChannel(PROJECT_ID, LOCATION, CHANNEL_ID);

    // Send a test stream for 45 seconds; needed to create a channel clip
    String[] command = {
      "ffmpeg",
      "-re",
      "-f",
      "lavfi",
      "-t",
      "45",
      "-i",
      "testsrc=size=1280x720 [out0]; sine=frequency=500 [out1]",
      "-vcodec",
      "h264",
      "-acodec",
      "aac",
      "-f",
      "flv",
      rtmpUri
    };
    ProcessBuilder pb = new ProcessBuilder(command);
    Process process = pb.start();
    int exitCode = process.waitFor();

    if (exitCode == 0) {
      System.out.println("FFmpeg command executed successfully.");
    } else {
      System.err.println("FFmpeg command failed with exit code: " + exitCode);
    }

    Clip response =
        CreateChannelClip.createChannelClip(
            PROJECT_ID, LOCATION, CHANNEL_ID, CLIP_ID, CLIP_OUTPUT_URI);
    assertThat(response.getName(), containsString(CLIP_NAME));

    bout.reset();
  }

  @Test
  public void test_GetChannelClip() throws Exception {
    Clip response = GetChannelClip.getChannelClip(PROJECT_ID, LOCATION, CHANNEL_ID, CLIP_ID);
    assertThat(response.getName(), containsString(CLIP_NAME));
  }

  @Test
  public void test_ListChannelClips() throws Exception {
    ListClipsPagedResponse response =
        ListChannelClips.listChannelClips(PROJECT_ID, LOCATION, CHANNEL_ID);
    Boolean pass = false;
    for (Clip clip : response.iterateAll()) {
      if (clip.getName().contains(CLIP_NAME)) {
        pass = true;
        break;
      }
    }
    assertTrue(pass);
  }

  @After
  public void tearDown() {
    bout.reset();
  }

  @AfterClass
  public static void afterTest()
      throws IOException, ExecutionException, InterruptedException, TimeoutException {
    StopChannel.stopChannel(PROJECT_ID, LOCATION, CHANNEL_ID);

    DeleteChannelClip.deleteChannelClip(PROJECT_ID, LOCATION, CHANNEL_ID, CLIP_ID);
    String deleteResponse = bout.toString();
    assertThat(deleteResponse, containsString("Deleted channel clip"));

    try {
      DeleteChannel.deleteChannel(PROJECT_ID, LOCATION, CHANNEL_ID);
    } catch (NotFoundException | InterruptedException | ExecutionException | TimeoutException e) {
      System.out.printf(String.valueOf(e));
    }

    try {
      DeleteInput.deleteInput(PROJECT_ID, LOCATION, INPUT_ID);
    } catch (NotFoundException | InterruptedException | ExecutionException | TimeoutException e) {
      System.out.printf(String.valueOf(e));
    }
    deleteBucket();

    System.out.flush();
    System.setOut(originalOut);
  }
}
