/*
 * Copyright 2023 Google LLC
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

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;

import com.google.cloud.bigtable.data.v2.BigtableDataClient;
import com.google.cloud.bigtable.data.v2.models.RowMutation;
import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import org.apache.commons.io.FileUtils;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

public class SongRankTest {

  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(3);

  // This table needs to be created manually before running the test since there is no API to create
  // change-stream enabled tables yet. For java-docs-samples, the table should already be created,
  // but if deleted, run the create table command in the README.
  private static final String TABLE_ID = "song-rank-test";
  private static final String COLUMN_FAMILY_NAME = "cf";
  private static final String COLUMN_NAME = "song";
  private static final String TEST_OUTPUT_LOCATION = "test-output/";

  private static String projectId;
  private static String instanceId;
  private ByteArrayOutputStream bout;

  private static String requireEnv(String varName) {
    String value = System.getenv(varName);
    assertNotNull(
        String.format("Environment variable '%s' is required to perform these tests.", varName),
        value);
    return value;
  }

  @BeforeClass
  public static void beforeClass() {
    projectId = requireEnv("GOOGLE_CLOUD_PROJECT");
    instanceId = requireEnv("BIGTABLE_TESTING_INSTANCE");
  }

  @Before
  public void setupStream() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @Test
  public void testSongRank() throws IOException, InterruptedException {
    String[] args = {"--bigtableProjectId=" + projectId, "--bigtableInstanceId=" + instanceId,
        "--bigtableTableId=" + TABLE_ID, "--outputLocation=" + TEST_OUTPUT_LOCATION};

    new Thread(() -> SongRank.main(args)).start();

    // Pause for job to start.
    Thread.sleep(10 * 1000);

    BigtableDataClient dataClient = BigtableDataClient.create(projectId, instanceId);
    String rowKey = "user-1234";
    String song1 = "song 1-" + UUID.randomUUID().toString().substring(0, 5);
    String song2 = "song 2-" + UUID.randomUUID().toString().substring(0, 5);

    for (int i = 0; i < 3; i++) {
      dataClient.mutateRow(
          RowMutation.create(TABLE_ID, rowKey).setCell(COLUMN_FAMILY_NAME, COLUMN_NAME, song1));
    }
    dataClient.mutateRow(
        RowMutation.create(TABLE_ID, rowKey).setCell(COLUMN_FAMILY_NAME, COLUMN_NAME, song2));

    // Wait for output to be written
    Thread.sleep(3 * 60 * 1000);

    String output = bout.toString();
    assertThat(output).contains("KV{" + song1 + ", 3}");
    assertThat(output).contains("KV{" + song2 + ", 1}");

    FileInputStream fis = new FileInputStream(
        TEST_OUTPUT_LOCATION + "/song-charts/GlobalWindow-pane-0-00000-of-00001.txt");
    byte[] data = new byte[(int) fis.available()];
    fis.read(data);
    String content = new String(data, StandardCharsets.UTF_8);
    assertThat(content).contains("KV{" + song1 + ", 3}");
    assertThat(content).contains("KV{" + song2 + ", 1}");

    FileUtils.deleteDirectory(new File(TEST_OUTPUT_LOCATION));
  }
}
