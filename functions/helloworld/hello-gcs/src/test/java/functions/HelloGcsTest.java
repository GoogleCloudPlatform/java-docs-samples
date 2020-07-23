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

// [START functions_storage_unit_test]
import static com.google.common.truth.Truth.assertThat;

import com.google.common.testing.TestLogHandler;
import functions.eventpojos.GcsEvent;
import functions.eventpojos.MockContext;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for main.java.com.example.functions.helloworld.HelloGcs.
 */
public class HelloGcsTest {
  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();
  private static final Logger logger = Logger.getLogger(HelloGcs.class.getName());

  @Before
  public void beforeTest() throws Exception {
    logger.addHandler(LOG_HANDLER);
  }

  @After
  public void afterTest() {
    LOG_HANDLER.clear();
  }

  @Test
  public void helloGcs_shouldPrintFileName() {
    GcsEvent event = new GcsEvent();
    event.setName("foo.txt");

    MockContext context = new MockContext();
    context.eventType = "google.storage.object.finalize";

    new HelloGcs().accept(event, context);

    String message = LOG_HANDLER.getStoredLogRecords().get(3).getMessage();
    assertThat(message).contains("File: foo.txt");
  }
}
// [END functions_storage_unit_test]
