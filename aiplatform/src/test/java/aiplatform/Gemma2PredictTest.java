/*
 * Copyright 2024 Google LLC
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.cloud.aiplatform.v1.EndpointName;
import com.google.cloud.aiplatform.v1.PredictResponse;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.protobuf.Value;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class Gemma2PredictTest {
  static String mockedResponse = "The sky appears blue due to a phenomenon "
      + "called **Rayleigh scattering**.\n"
      + "**Here's how it works:**\n"
      + "* **Sunlight is white:**  Sunlight actually contains all the colors of the rainbow.\n"
      + "* **Scattering:** When sunlight enters the Earth's atmosphere, it collides with tiny gas"
      + " molecules (mostly nitrogen and oxygen). These collisions cause the light to scatter "
      + "in different directions.\n"
      + "* **Blue light scatters most:**  Blue light has a shorter wavelength";
  String projectId = "your-project-id";
  String region = "us-central1";
  String endpointId = "your-endpoint-id";
  static PredictionServiceClient mockPredictionServiceClient;

  @BeforeAll
  public static void setUp() {
    // Mock PredictionServiceClient and its response
    mockPredictionServiceClient = Mockito.mock(PredictionServiceClient.class);
    PredictResponse predictResponse =
        PredictResponse.newBuilder()
            .addPredictions(Value.newBuilder().setStringValue(mockedResponse).build())
            .build();
    Mockito.when(mockPredictionServiceClient.predict(
                Mockito.any(EndpointName.class),
                Mockito.any(List.class),
                Mockito.any(Value.class)))
        .thenReturn(predictResponse);
  }

  @Test
  public void testGemma2PredictTpu() throws IOException {
    Gemma2PredictTpu creator = new Gemma2PredictTpu(mockPredictionServiceClient);
    String response = creator.gemma2PredictTpu(projectId, region, endpointId);

    assertEquals(mockedResponse, response);
  }

  @Test
  public void testGemma2PredictGpu() throws IOException {
    Gemma2PredictGpu creator = new Gemma2PredictGpu(mockPredictionServiceClient);
    String response = creator.gemma2PredictGpu(projectId, region, endpointId);

    assertEquals(mockedResponse, response);
  }
}
