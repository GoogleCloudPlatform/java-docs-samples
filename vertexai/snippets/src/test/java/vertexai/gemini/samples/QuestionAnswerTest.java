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
package vertexai.gemini.samples;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

@RunWith(JUnit4.class)
public class QuestionAnswerTest {
    private static final String GOOGLE_CLOUD_PROJECT = "GOOGLE_CLOUD_PROJECT";

    @BeforeClass
    public static void setUp() {
        assertWithMessage(String.format("Missing environment variable '%s' ", GOOGLE_CLOUD_PROJECT))
            .that(System.getenv(GOOGLE_CLOUD_PROJECT))
            .isNotEmpty();
    }

    @Test
    public void simpleQuestion() throws Exception {
        String output = QuestionAnswer.simpleQuestion();
        System.out.println(output);

        assertThat(output).isNotEmpty();
        assertThat(output).contains("Rayleigh scattering");
    }
}
