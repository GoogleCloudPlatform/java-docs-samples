/*
 * Copyright 2025 Google Inc.
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

package com.example.speech;
import com.google.cloud.speech.v2.StreamingRecognizeResponse;
import com.google.common.truth.Truth;
import org.junit.Test;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TranscribeStreamingV2IT {

    @Test
    public void testTranscribeStreamingV2_Success() throws IOException {
        // Create a dummy audio file for testing
        String testFilePath = "./resources/brooklyn_bridge.wav";

        // Call the method to test
        List<StreamingRecognizeResponse> responses = TranscribeStreamingV2.transcribeStreamingV2(testFilePath);

        // Assert the transcript
        String transcript = "";
        for (StreamingRecognizeResponse response : responses) {
            if (response.getResultsCount() > 0) {
                transcript += response.getResults(0).getAlternatives(0).getTranscript();
            }
        }
        // Use a regex to match the expected transcript
        Pattern pattern = Pattern.compile("how old is the Brooklyn Bridge", Pattern.CASE_INSENSITIVE | Pattern.DOTALL);
        Truth.assertThat(pattern.matcher(transcript).find()).isTrue();

    }
}

