/*
 * Copyright 2025 Google LLC
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

// [START TranscribeStreamingV2]

import com.google.api.gax.rpc.BidiStream;
import com.google.cloud.speech.v2.*;
import com.google.protobuf.ByteString;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class TranscribeStreamingV2 {
    private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");

    //    Transcribes audio from an audio file stream using Google Cloud Speech-to-Text API.
//    Args:
//        streamFile (String): Path to the local audio file to be transcribed.
//            Example: "resources/audio.wav"
//    Returns:
//        List<StreamingRecognizeResponse>: A list of objects.
//            Each response includes the transcription results for the corresponding audio segment.
//
    public static List<StreamingRecognizeResponse> transcribeStreamingV2(String streamFile) throws IOException {
        List<StreamingRecognizeResponse> responses = new ArrayList<>();
//      Instantiates a client
        try (SpeechClient client = SpeechClient.create()) {

            Path path = Paths.get(streamFile);
            byte[] audioContent = Files.readAllBytes(path);

//          In practice, stream should be a generator yielding chunks of audio data
            int chunkLength = audioContent.length / 5;
            List<byte[]> stream = new ArrayList<>();
            for (int i = 0; i < audioContent.length; i += chunkLength) {
                int end = Math.min(i + chunkLength, audioContent.length);
                byte[] chunk = new byte[end - i];
                System.arraycopy(audioContent, i, chunk, 0, end - i);
                stream.add(chunk);
            }


            List<StreamingRecognizeRequest> audioRequests = new ArrayList<>();
            for (byte[] audio : stream) {
                audioRequests.add(StreamingRecognizeRequest.newBuilder().setAudio(ByteString.copyFrom(audio)).build());
            }

            RecognitionConfig recognitionConfig = RecognitionConfig.newBuilder()
                    .setAutoDecodingConfig(AutoDetectDecodingConfig.getDefaultInstance())
                    .addLanguageCodes("en-US")
                    .setModel("long")
                    .build();

            StreamingRecognitionConfig streamingConfig = StreamingRecognitionConfig.newBuilder()
                    .setConfig(recognitionConfig)
                    .build();

            StreamingRecognizeRequest configRequest = StreamingRecognizeRequest.newBuilder()
                    .setRecognizer(String.format("projects/%s/locations/global/recognizers/_", PROJECT_ID))
                    .setStreamingConfig(streamingConfig)
                    .build();


            List<StreamingRecognizeRequest> requests = new ArrayList<>();
            requests.add(configRequest);
            requests.addAll(audioRequests);

            BidiStream<StreamingRecognizeRequest, StreamingRecognizeResponse> stream1 = client.streamingRecognizeCallable().call();
            for (StreamingRecognizeRequest request : requests) {
                stream1.send(request);
            }
            for (int i = 0; i < requests.size(); i++) {
                StreamingRecognizeRequest request = requests.get(i);
                System.out.println("Request " + (i + 1) + ": " + request);
                // You might want to print specific parts of the request, like the audio or config, rather than the entire object
                if (request.hasAudio()) {
                    System.out.println("Audio: " + request.getAudio().size() + " bytes");
                }
                if (request.hasStreamingConfig()) {
                    System.out.println("Streaming Config: " + request.getStreamingConfig());
                }
            }
            stream1.closeSend();

            Iterator<StreamingRecognizeResponse> responseIterator = stream1.iterator();
            while (responseIterator.hasNext()) {
                StreamingRecognizeResponse response = responseIterator.next();
                System.out.println(response);
                // Process the response and extract the transcript
                System.out.println("Transcript: " + response.getResultsList().get(0).getAlternativesList().get(0).getTranscript());
                responses.add(response);
            }


        }
        return responses;
    }

    public static void main(String[] args) throws IOException {
        List<StreamingRecognizeResponse> responses = transcribeStreamingV2("./resources/brooklyn_bridge.wav");
    }
}
// [END TranscribeStreamingV2]
