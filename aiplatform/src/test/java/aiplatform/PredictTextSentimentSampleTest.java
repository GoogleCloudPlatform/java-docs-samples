/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package aiplatform;

import static com.google.common.truth.Truth.assertThat;
import static junit.framework.TestCase.assertNotNull;

import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

public class PredictTextSentimentSampleTest {
  @Rule public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(3);

  private static final String PROJECT = System.getenv("UCAIP_PROJECT_ID");
  private static final String INSTANCE =
      "{ \"content\": \"I had to compare two versions of Hamlet for my Shakespeare \n"
          + "class and unfortunately I picked this version. Everything from the acting \n"
          + "(the actors deliver most of their lines directly to the camera) to the camera \n"
          + "shots (all medium or close up shots...no scenery shots and very little back \n"
          + "ground in the shots) were absolutely terrible. I watched this over my spring \n"
          + "break and it is very safe to say that I feel that I was gypped out of 114 \n"
          + "minutes of my vacation. Not recommended by any stretch of the imagination.\n"
          + "Classify the sentiment of the message: negative\n"
          + "\n"
          + "Something surprised me about this movie - it was actually original. It was \n"
          + "not the same old recycled crap that comes out of Hollywood every month. I saw \n"
          + "this movie on video because I did not even know about it before I saw it at my \n"
          + "local video store. If you see this movie available - rent it - you will not \n"
          + "regret it.\n"
          + "Classify the sentiment of the message: positive\n"
          + "\n"
          + "My family has watched Arthur Bach stumble and stammer since the movie first \n"
          + "came out. We have most lines memorized. I watched it two weeks ago and still \n"
          + "get tickled at the simple humor and view-at-life that Dudley Moore portrays. \n"
          + "Liza Minelli did a wonderful job as the side kick - though I'm not her \n"
          + "biggest fan. This movie makes me just enjoy watching movies. My favorite scene \n"
          + "is when Arthur is visiting his fiancée's house. His conversation with the \n"
          + "butler and Susan's father is side-spitting. The line from the butler, \n"
          + "\\\"Would you care to wait in the Library\\\" followed by Arthur's reply, \n"
          + "\\\"Yes I would, the bathroom is out of the question\\\", is my NEWMAIL \n"
          + "notification on my computer.\n"
          + "Classify the sentiment of the message: positive\n"
          + "\n"
          + "This Charles outing is decent but this is a pretty low-key performance. Marlon \n"
          + "Brando stands out. There's a subplot with Mira Sorvino and Donald Sutherland \n"
          + "that forgets to develop and it hurts the film a little. I'm still trying to \n"
          + "figure out why Charlie want to change his name.\n"
          + "Classify the sentiment of the message: negative\n"
          + "\n"
          + "Tweet: The Pixel 7 Pro, is too big to fit in my jeans pocket, so I bought new \n"
          + "jeans.\n"
          + "Classify the sentiment of the message: \"}";
  private static final String PARAMETERS =
      "{\n"
          + "  \"temperature\": 0,\n"
          + "  \"maxDecodeSteps\": 5,\n"
          + "  \"topP\": 0,\n"
          + "  \"topK\": 1\n"
          + "}";
  private static final String PUBLISHER = "google";
  private static final String LOCATION = "us-central1";
  private static final String MODEL = "text-bison@001";

  private ByteArrayOutputStream bout;
  private PrintStream out;
  private PrintStream originalPrintStream;

  private static void requireEnvVar(String varName) {
    String errorMessage =
        String.format("Environment variable '%s' is required to perform these tests.", varName);
    assertNotNull(errorMessage, System.getenv(varName));
  }

  @BeforeClass
  public static void checkRequirements() {
    requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
    requireEnvVar("UCAIP_PROJECT_ID");
  }

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    originalPrintStream = System.out;
    System.setOut(out);
  }

  @After
  public void tearDown() {
    System.out.flush();
    System.setOut(originalPrintStream);
  }

  @Test
  public void testPredictTextSentiment() throws IOException {
    // Act
    PredictTextSentimentSample.predictTextSentiment(
        INSTANCE, PARAMETERS, PROJECT, LOCATION, PUBLISHER, MODEL);

    // Assert
    String got = bout.toString();
    assertThat(got).contains("Predict Response");
  }
}
