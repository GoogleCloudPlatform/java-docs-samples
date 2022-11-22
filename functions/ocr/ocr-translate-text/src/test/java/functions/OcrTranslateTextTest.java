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

import com.google.common.testing.TestLogHandler;
import com.google.common.truth.Truth;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import functions.eventpojos.PubSubMessage;
import java.io.IOException;
import java.util.Base64;
import java.util.List;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class OcrTranslateTextTest {
  private static final Logger logger = Logger.getLogger(
      OcrTranslateText.class.getName());

  private static final TestLogHandler LOG_HANDLER = new TestLogHandler();

  private static final Gson gson = new Gson();

  private static OcrTranslateText sampleUnderTest;

  @BeforeClass
  public static void setUpClass() throws IOException {
    sampleUnderTest = new OcrTranslateText();
    logger.addHandler(LOG_HANDLER);
  }

  @After
  public void afterTest() {
    LOG_HANDLER.clear();
  }

  @Test(expected = IllegalArgumentException.class)
  public void functionsOcrTranslate_shouldValidateParams() throws IOException {
    PubSubMessage message = new PubSubMessage();
    message.setData(new String(Base64.getEncoder().encode("{}".getBytes())));

    sampleUnderTest.accept(message, null); // must be in a variable to avoid GC issues
  }

  @Test
  public void functionsOcrTranslate_shouldTranslateText() throws IOException {
    String text = "Wake up human!";
    String filename = "wakeupcat.jpg";
    String lang = "es";

    JsonObject dataJson = new JsonObject();
    dataJson.addProperty("text", text);
    dataJson.addProperty("filename", filename);
    dataJson.addProperty("lang", lang);

    PubSubMessage message = new PubSubMessage();
    message.setData(new String(Base64.getEncoder().encode(gson.toJson(dataJson).getBytes())));

    sampleUnderTest.accept(message, null); // must be in a variable to avoid GC issues

    List<LogRecord> logs = LOG_HANDLER.getStoredLogRecords();
    Truth.assertThat(logs.get(1).getMessage()).contains("¡Despierta humano!");
    Truth.assertThat(logs.get(2).getMessage()).isEqualTo("Text translated to es");
  }
}
