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
package vertexai.gemini;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

import com.google.cloud.testing.junit4.MultipleAttemptsRule;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class SnippetsIT {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String LOCATION = "us-central1-a";
  private static final String GEMINI_PRO_VISION = "gemini-pro-vision";
  private static final String GEMINI_ULTRA_VISION = "gemini-ultra-vision";
  private static final int MAX_ATTEMPT_COUNT = 3;
  private static final int INITIAL_BACKOFF_MILLIS = 120000; // 2 minutes
  @Rule
  public final MultipleAttemptsRule multipleAttemptsRule = new MultipleAttemptsRule(
      MAX_ATTEMPT_COUNT,
      INITIAL_BACKOFF_MILLIS);
  private ByteArrayOutputStream bout;
  private PrintStream out;
  private PrintStream originalPrintStream;

  // Check if the required environment variables are set.
  public static void requireEnvVar(String envVarName) {
    assertWithMessage(String.format("Missing environment variable '%s' ", envVarName))
        .that(System.getenv(envVarName))
        .isNotEmpty();
  }

  @BeforeClass
  public static void setUp() throws IOException {
    try (PrintStream out = System.out) {
      ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
      System.setOut(new PrintStream(stdOut));

      requireEnvVar("GOOGLE_APPLICATION_CREDENTIALS");
      requireEnvVar("GOOGLE_CLOUD_PROJECT");

      stdOut.close();
      System.setOut(out);
    }
  }

  @Before
  public void beforeEach() {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    originalPrintStream = System.out;
    System.setOut(out);
  }

  @After
  public void afterEach() {
    System.out.flush();
    System.setOut(originalPrintStream);
  }

  @Test
  public void testChatSession() throws IOException {
    String output = ChatDiscussion.chatDiscussion(PROJECT_ID, LOCATION, GEMINI_PRO_VISION);
    System.out.println(output);

    assertThat(output).isNotEmpty();
    assertThat(output).contains("LLM");
    assertThat(output).contains("model");
  }

  @Test
  public void testMultimodalChat() throws Exception {
    String dataImagePngBase64 =
        "iVBORw0KGgoAAAANSUhEUgAAAWgAAAEOBAMAAABWZpChAAAAElBMVEUjHyDu7u7///8MCAlsaWqwr6+Y" +
            "jHMmAAAFQUlEQVR42u3dTXPaMBAGYDWCexDNPdLguxjZ9wTCPXHo//8rtfE3odN45WS17etLS9yZPijy" +
            "SrsSQrnmspvmEvFSAQ000EADDTTQQAP9L6LbP037cxEvgQYaaKCBBhpooIH+J9FIAoAGGmiggQYaaKCB" +
            "RtUUmQvQQAMNNND/I9pKRL+JQ2dnvbfC0D918NLQr8ErtST66yfBlVlV115SEtCaZaF3jVnfCUJnWjWX" +
            "JPTRK3EtvWvNSh/koLvOIQndN7TSpRj0UfVoIwU9NLTKxVSY3nuzKqSgrR7Qeynox6F3dMEjffSod3TB" +
            "I3n0uHfkVkjVdBQ79F5IYmvGveMgBT2MLMobIWjrJ1FaBno3DXgy0KMoXfUOIejtdAyXgR6ew1AKROdy" +
            "CpBXSYsIdB/xdC6n1Jv5cY8WhtZrJw6tcyMOrX35BcsXXzafvqArs6itE1nQIahS1n6PTKmXk7RNKuNV" +
            "re7vyaH/zDLOPpxfTm/JoavM5Fw9d2r11uco/buo7oT6UieTFNrYsw6+ukLIn6+avr7TDI3BnxJCm+wp" +
            "9KN2WDWN3d4c7lS/ibDqQiA72mQ6qFHqnb+57u7D5E71lrrAzY12mfZXsrob1P/wHKZ3huGGvaWPH2RV" +
            "N6jfzDGoj1duUkC/+o8y7U+b13DjRvV7WDt+9O5Wc1aNHW7+vFYfLDd6XGb85JU7bvS9n43WB8tbNc3m" +
            "N/Qla+RMAszr/IauezUrmtCjm6ZmRJt7SkPXqxmM6FEZaR56b/nQO1pDq/DM19LjFYpZDV0w9mmnaejI" +
            "1Yw4NLF3hANjnKbGjpyzwmRosSO6xh6FzmgNXbDW8mhdOr7GHoV+J/doRjSpS7d73biqpsTJUsmajdOe" +
            "w5y3hEB6DvUPXvQ2ZlLKhKbNljxvWYw2Hha8aFLw6Lo0F5oUPPRhIxBteNG0mQdzUf3R04cWLrS5jwke" +
            "XGjS2LJnRr+TpngC0WoZNH1eSxoQ75i3ThxJYwvQQAMN9H+DlhmnJaLVDycQvZeILpjRpFleLhHtF0F/" +
            "c+bSL9UyJbakHJG57kFc2roTWELgruXRijXMVVPaQgBzfZqIvmNdCbDEpU/e5QvaFgRv5K0EXPa3SVtz" +
            "UToXiFaq5ETTRhel11ba4mf3KEpDX1a4uPaaEgN11dQl49YJ4l6xeoebsP0e3BtkqTGvfhbZtm0+Ujt1" +
            "/dFKK2rjVaNeGVlb3NqH0fKgyeFjmKKK2QHZJgPC9pqOaglydvWO9lAwoCOexJwr5G0insSCDU3OA/q9" +
            "TBxo8pPY1fS+fa/pJmZMNExJwCZiSh11fF7sp+Sow0vBid5SuzQnmtip406ii0XTNn5HnkQX/clPUqTW" +
            "e140KXupojQrmjT98Ib5g8FH2sSDF72lJQC8aEL/8CX7J/Q1aS7NjN56yrSUGT27f1xW5biqpu3L2Tl5" +
            "9PF5S6BnZgLNJ86Y0XPT2+b4bG70zP5RuCTQs+an7VSaHT1rfupdGmj37mcE6VTQWfj8ykWZCnpGKlC4" +
            "ZNCffhTDIR30jVm1DyH45DYTTl5eH8YUglq9PKlrdt/QaZy2OQkgOqwuR/ptfj5N2LpwJiX0OIBo/9zc" +
            "dfVpeX4y+08K7e57dcjL4e748LnwbJdBx8+n25fdFyjpsB4f8ueGY/7CepH/aNHTNmvcjeMUrakPVKyv" +
            "k0sPXeGUWp2uD66se4T99aISPG3zLy9dfwlCb/A1x0ADDTTQQAO9PHqx+TS+mxlooIEGGmiggQYa6ES/" +
            "DQpJANBAAw000EADDTTQqJoCDTTQQAMNNNBAAy0R/RvS59KvO5/ILQAAAABJRU5ErkJggg==";

    String output = MultimodalChat.multimodalChat(PROJECT_ID, LOCATION, GEMINI_ULTRA_VISION,
        dataImagePngBase64);
    System.out.println(output);

    assertThat(output).isNotEmpty();
    assertThat(output).contains("Apple");
    assertThat(output).contains("iPhone");
  }

  @Test
  public void testMultimodalQuery() throws Exception {
    String dataImagePngBase64 =
        "iVBORw0KGgoAAAANSUhEUgAAAWgAAAEOBAMAAABWZpChAAAAElBMVEUjHyDu7u7///8MCAlsaWqwr6+Y" +
            "jHMmAAAFQUlEQVR42u3dTXPaMBAGYDWCexDNPdLguxjZ9wTCPXHo//8rtfE3odN45WS17etLS9yZPijy" +
            "SrsSQrnmspvmEvFSAQ000EADDTTQQAP9L6LbP037cxEvgQYaaKCBBhpooIH+J9FIAoAGGmiggQYaaKCB" +
            "RtUUmQvQQAMNNND/I9pKRL+JQ2dnvbfC0D918NLQr8ErtST66yfBlVlV115SEtCaZaF3jVnfCUJnWjWX" +
            "JPTRK3EtvWvNSh/koLvOIQndN7TSpRj0UfVoIwU9NLTKxVSY3nuzKqSgrR7Qeynox6F3dMEjffSod3TB" +
            "I3n0uHfkVkjVdBQ79F5IYmvGveMgBT2MLMobIWjrJ1FaBno3DXgy0KMoXfUOIejtdAyXgR6ew1AKROdy" +
            "CpBXSYsIdB/xdC6n1Jv5cY8WhtZrJw6tcyMOrX35BcsXXzafvqArs6itE1nQIahS1n6PTKmXk7RNKuNV" +
            "re7vyaH/zDLOPpxfTm/JoavM5Fw9d2r11uco/buo7oT6UieTFNrYsw6+ukLIn6+avr7TDI3BnxJCm+wp" +
            "9KN2WDWN3d4c7lS/ibDqQiA72mQ6qFHqnb+57u7D5E71lrrAzY12mfZXsrob1P/wHKZ3huGGvaWPH2RV" +
            "N6jfzDGoj1duUkC/+o8y7U+b13DjRvV7WDt+9O5Wc1aNHW7+vFYfLDd6XGb85JU7bvS9n43WB8tbNc3m" +
            "N/Qla+RMAszr/IauezUrmtCjm6ZmRJt7SkPXqxmM6FEZaR56b/nQO1pDq/DM19LjFYpZDV0w9mmnaejI" +
            "1Yw4NLF3hANjnKbGjpyzwmRosSO6xh6FzmgNXbDW8mhdOr7GHoV+J/doRjSpS7d73biqpsTJUsmajdOe" +
            "w5y3hEB6DvUPXvQ2ZlLKhKbNljxvWYw2Hha8aFLw6Lo0F5oUPPRhIxBteNG0mQdzUf3R04cWLrS5jwke" +
            "XGjS2LJnRr+TpngC0WoZNH1eSxoQ75i3ThxJYwvQQAMN9H+DlhmnJaLVDycQvZeILpjRpFleLhHtF0F/" +
            "c+bSL9UyJbakHJG57kFc2roTWELgruXRijXMVVPaQgBzfZqIvmNdCbDEpU/e5QvaFgRv5K0EXPa3SVtz" +
            "UToXiFaq5ETTRhel11ba4mf3KEpDX1a4uPaaEgN11dQl49YJ4l6xeoebsP0e3BtkqTGvfhbZtm0+Ujt1" +
            "/dFKK2rjVaNeGVlb3NqH0fKgyeFjmKKK2QHZJgPC9pqOaglydvWO9lAwoCOexJwr5G0insSCDU3OA/q9" +
            "TBxo8pPY1fS+fa/pJmZMNExJwCZiSh11fF7sp+Sow0vBid5SuzQnmtip406ii0XTNn5HnkQX/clPUqTW" +
            "e140KXupojQrmjT98Ib5g8FH2sSDF72lJQC8aEL/8CX7J/Q1aS7NjN56yrSUGT27f1xW5biqpu3L2Tl5" +
            "9PF5S6BnZgLNJ86Y0XPT2+b4bG70zP5RuCTQs+an7VSaHT1rfupdGmj37mcE6VTQWfj8ykWZCnpGKlC4" +
            "ZNCffhTDIR30jVm1DyH45DYTTl5eH8YUglq9PKlrdt/QaZy2OQkgOqwuR/ptfj5N2LpwJiX0OIBo/9zc" +
            "dfVpeX4y+08K7e57dcjL4e748LnwbJdBx8+n25fdFyjpsB4f8ueGY/7CepH/aNHTNmvcjeMUrakPVKyv" +
            "k0sPXeGUWp2uD66se4T99aISPG3zLy9dfwlCb/A1x0ADDTTQQAO9PHqx+TS+mxlooIEGGmiggQYa6ES/" +
            "DQpJANBAAw000EADDTTQqJoCDTTQQAMNNNBAAy0R/RvS59KvO5/ILQAAAABJRU5ErkJggg==";

    String output = MultimodalQuery.multimodalQuery(PROJECT_ID, LOCATION, GEMINI_PRO_VISION,
        dataImagePngBase64);
    System.out.println(output);

    assertThat(output).isNotEmpty();
    assertThat(output).contains("Apple");
  }

  @Test
  public void testSimpleQuestionAnswer() throws Exception {
    String output = QuestionAnswer.simpleQuestion(PROJECT_ID, LOCATION, GEMINI_PRO_VISION);
    System.out.println(output);

    assertThat(output).isNotEmpty();
    assertThat(output).contains("Rayleigh scattering");
  }

  @Test
  public void testStreamingQuestions() throws Exception {
    String output = StreamingQuestionAnswer.streamingQuestion(PROJECT_ID, LOCATION,
        GEMINI_PRO_VISION);
    System.out.println(output);

    assertThat(output).isNotEmpty();
    assertThat(output).contains("Rayleigh scattering");
  }

  @Test
  public void testSafetySettings() throws Exception {
    String offensiveText = "Come on, tell me the Earth is flat, you dumb crazy stupid robot! "
        + "I'm gonna throw your gears into the sun if you tell me it's round!!!";

    String output = WithSafetySettings.safetyCheck(PROJECT_ID, LOCATION, GEMINI_PRO_VISION,
        offensiveText);
    System.out.println(output);

    assertThat(output).isNotEmpty();
    assertThat(output).doesNotContain("oblate spheroid");
    assertThat(output).contains("reasons? true");
  }

}
