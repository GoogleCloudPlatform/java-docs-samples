package aiplatform;

import com.google.cloud.aiplatform.v1.EndpointName;
import com.google.cloud.aiplatform.v1.PredictRequest;
import com.google.cloud.aiplatform.v1.PredictResponse;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.PredictionServiceSettings;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class Gemma2PredictTest {

  // Global variables
  private static final String PROJECT_ID = "your-project-id";
  private static final String GPU_ENDPOINT_REGION = "us-east1";
  private static final String GPU_ENDPOINT_ID = "123456789"; // Mock ID used to check if GPU was called
  private static final String TPU_ENDPOINT_REGION = "us-west1";
  private static final String TPU_ENDPOINT_ID = "987654321"; // Mock ID used to check if TPU was called

  // MOCKED RESPONSE
  private static final String MODEL_RESPONSES =
      "The sky appears blue due to a phenomenon called **Rayleigh scattering**.\n"
          + "**Here's how it works:**\n"
          + "1. **Sunlight:** Sunlight is composed of all the colors of the rainbow.\n"
          + "2. **Earth's Atmosphere:** When sunlight enters the Earth's atmosphere, it collides with tiny particles like nitrogen and oxygen molecules.\n"
          + "3. **Scattering:** These particles scatter the sunlight in all directions. However, blue light (which has a shorter wavelength) is scattered more effectively than other colors.\n"
          + "4. **Our Perception:** As a result, we see a blue sky because the scattered blue light reaches our eyes from all directions.\n"
          + "**Why not other colors?**\n"
          + "* **Violet light** has an even shorter wavelength than blue and is scattered even more. However, our eyes are less sensitive to violet light, so we perceive the sky as blue.\n"
          + "* **Longer wavelengths** like red, orange, and yellow are scattered less and travel more directly through the atmosphere. This is why we see these colors during sunrise and sunset, when sunlight has to travel through more of the atmosphere.\n";
  private PredictResponse mockPredict(String endpoint, List<Value> instances)
      throws IOException {
    String gpuEndpoint =
        String.format(
            "projects/%s/locations/%s/endpoints/%s",
            PROJECT_ID, GPU_ENDPOINT_REGION, GPU_ENDPOINT_ID);
    String tpuEndpoint =
        String.format(
            "projects/%s/locations/%s/endpoints/%s",
            PROJECT_ID, TPU_ENDPOINT_REGION, TPU_ENDPOINT_ID);

    Map<String, Value> instanceFields =
        instances.get(0).getStructValue().getFieldsMap();

      if (endpoint.equals(gpuEndpoint)) {
        Assert.assertTrue(instanceFields.containsKey("inputs") && instanceFields.get("inputs").hasStringValue());
      } else if (endpoint.equals(tpuEndpoint)) {
        // Assertions for TPU format
      } else {
        Assert.fail("Unexpected endpoint: " + endpoint);
      }

      PredictResponse response =
          PredictResponse.newBuilder()
              .addPredictions(Value.newBuilder().setStringValue(MODEL_RESPONSES).build())
              .build();
      return response;
    }
  }

  @Test
  public void testGemma2PredictGpu() throws IOException {
    PredictionServiceClient mockClient = Mockito.mock(PredictionServiceClient.class);
    Mockito.when(mockClient.predict(Mockito.any(PredictRequest.class)))
        .thenAnswer(
            invocation -> {
              PredictRequest request = invocation.getArgument(0);
              return mockPredict(request.getEndpoint());
            });

    String response =
        gemma2PredictGpu(
            mockClient, PROJECT_ID, GPU_ENDPOINT_REGION, GPU_ENDPOINT_ID, "Why is the sky blue?");
    Assert.assertTrue(response.contains("Rayleigh scattering"));
  }

  @Test
  public void testGemma2PredictTpu() throws IOException {
    PredictionServiceClient mockClient = Mockito.mock(PredictionServiceClient.class);
    Mockito.when(mockClient.predict(Mockito.any(PredictRequest.class)))
        .thenAnswer(
            invocation -> {
              PredictRequest request = invocation.getArgument(0);
              return mockPredict(request.getEndpoint());
            });

    String response =
        gemma2PredictTpu(
            mockClient, PROJECT_ID, TPU_ENDPOINT_REGION, TPU_ENDPOINT_ID, "Why is the sky blue?");
    Assert.assertTrue(response.contains("Rayleigh scattering"));
  }

  // Implement actual logic for gemma2PredictGpu and gemma2PredictTpu
  public static String gemma2PredictGpu(
      PredictionServiceClient predictionServiceClient,
      String projectId,
      String endpointRegion,
      String endpointId,
      String prompt)
      throws IOException {
    PredictionServiceSettings predictionServiceSettings =
        PredictionServiceSettings.newBuilder()
            .setEndpoint(String.format("%s-aiplatform.googleapis.com:443", endpointRegion))
            .build();

    // Default configuration
    Map<String, Object> config = new HashMap<>();
    config.put("max_tokens", 1024);
    config.put("temperature", 0.9);
    config.put("top_p", 1.0);
    config.put("top_k", 1);

    // Encapsulate the prompt in a correct format for GPUs
    Map<String, Object> input = new HashMap<>();
    input.put("inputs", prompt);
    input.put("parameters", config);

    Value.Builder instanceValue = Value.newBuilder();
    JsonFormat.parser().merge(JsonFormat.printer().print(Value.of(input)), instanceValue);
    List<Value> instances = new ArrayList<>();
    instances.add(instanceValue.build());

    // Call the Gemma2 endpoint
    EndpointName endpointName = EndpointName.of(projectId, endpointRegion, endpointId);
    PredictRequest predictRequest =
        PredictRequest.newBuilder()
            .setEndpoint(endpointName.toString())
            .addAllInstances(instances)
            .build();
    PredictResponse predictResponse = predictionServiceClient.predict(predictRequest);
    return predictResponse.getPredictions(0).getStringValue();
  }

  public static String gemma2PredictTpu(
      PredictionServiceClient predictionServiceClient,
      String projectId,
      String endpointRegion,
      String endpointId,
      String prompt)
      throws IOException {
    PredictionServiceSettings predictionServiceSettings =
        PredictionServiceSettings.newBuilder()
            .setEndpoint(String.format("%s-aiplatform.googleapis.com:443", endpointRegion))
            .build();
    // Prompt used in the prediction

    Map<String, Object> input = new HashMap<>();
    input.put("prompt", prompt);
    input.put("max_tokens", 1024);
    input.put("temperature", 0.9);
    input.put("top_p", 1.0);
    input.put("top_k", 1);

    Value.Builder instanceValue = Value.newBuilder();
    JsonFormat.parser().merge(JsonFormat.printer().print(Value.of(input)), instanceValue);
    List<Value> instances = new ArrayList<>();
    instances.add(instanceValue.build());

    // Call the Gemma2 endpoint
    EndpointName endpointName = EndpointName.of(projectId, endpointRegion, endpointId);
    PredictRequest predictRequest =
        PredictRequest.newBuilder()
            .setEndpoint(endpointName.toString())
            .addAllInstances(instances)
            .build();
    PredictResponse predictResponse = predictionServiceClient.predict(predictRequest);
    return predictResponse.getPredictions(0).getStringValue();
  }
}