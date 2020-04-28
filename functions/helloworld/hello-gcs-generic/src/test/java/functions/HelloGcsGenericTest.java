/*
 * Copyright 2020 Google LLC
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

package functions;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.TestLogHandler;
import functions.eventpojos.GcsEvent;
import functions.eventpojos.MockContext;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class HelloGcsGenericTest {
  // Must be declared at class-level, or LoggingHandler won't detect log records!
  private static final Logger logger = Logger.getLogger(HelloGcsGeneric.class.getName());

  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  @BeforeClass
  public static void beforeClass() {
    logger.addHandler(LOG_HANDLER);
  }

  @Test
  public void functionsHelloworldStorageGeneric_shouldPrintEvent() throws IOException {
    GcsEvent event = new GcsEvent();
    event.setBucket("some-bucket");
    event.setName("some-file.txt");
    event.setTimeCreated(new Date());
    event.setUpdated(new Date());

    MockContext context = new MockContext();
    context.eventType = "google.storage.object.metadataUpdate";

    new HelloGcsGeneric().accept(event, context);

    List<LogRecord> logs = LOG_HANDLER.getStoredLogRecords();
    assertThat(logs.get(1).getMessage()).isEqualTo(
        "Event Type: google.storage.object.metadataUpdate");
    assertThat(logs.get(2).getMessage()).isEqualTo("Bucket: some-bucket");
    assertThat(logs.get(3).getMessage()).isEqualTo("File: some-file.txt");
  }
}
