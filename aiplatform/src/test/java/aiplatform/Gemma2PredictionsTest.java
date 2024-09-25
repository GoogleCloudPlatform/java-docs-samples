package aiplatform;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import com.google.cloud.aiplatform.v1.PredictResponse;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.stub.PredictionServiceStub;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Gemma2PredictionsTest {

  private static final String RESPONSE = "The sky appears blue due to a phenomenon called **Rayleigh scattering**.\n" +
      "**Here's how it works:**\n" +
      "1. **Sunlight:** Sunlight is composed of all the colors of the rainbow.\n" +
      "2. **Earth's Atmosphere:** When sunlight enters the Earth's atmosphere, it collides with tiny particles like nitrogen and oxygen molecules.\n" +
      "3. **Scattering:** These particles scatter the sunlight in all directions. However, blue light (which has a shorter wavelength) is scattered more effectively than other colors.\n" +
      "4. **Our Perception:** As a result, we see a blue sky because the scattered blue light reaches our eyes from all directions.\n" +
      "**Why not other colors?**\n" +
      "* **Violet light** has an even shorter wavelength than blue and is scattered even more. However, our eyes are less sensitive to violet light, so we perceive the sky as blue.\n" +
      "* **Longer wavelengths** like red, orange, and yellow are scattered less and travel more directly through the atmosphere. This is why we see these colors during sunrise and sunset, when sunlight has to travel through more of the atmosphere.\n";

  private static final String PROJECT_ID = "rsamborski-ai-hypercomputer";
  private static final String GPU_ENDPOINT_REGION = "us-east1";
  private static final String GPU_ENDPOINT_ID = "323876543124209664"; // Mock ID used to check if GPU was called
  private static final String TPU_ENDPOINT_REGION = "us-west1";
  private static final String TPU_ENDPOINT_ID = "9194824316951199744";
  private static final String PARAMETERS =
      "{\n"
          + "  \"temperature\": 0.3,\n"
          + "  \"maxOutputTokens\": 200,\n"
          + "  \"topP\": 0.8,\n"
          + "  \"topK\": 40\n"
          + "}";
  private final  PredictionServiceStub mockStub = Mockito.mock(PredictionServiceStub.class);
  PredictionServiceClient client = PredictionServiceClient.create(mockStub);

  @Test
  public void testShouldRunInterferenceWithGPU() throws IOException {
     PredictionServiceStub mockStub = Mockito.mock(PredictionServiceStub.class);
    PredictionServiceClient client = PredictionServiceClient.create(mockStub);
    String instance = "{ \"inputs\": \"Why is the sky blue?\"}";
    Value.Builder instanceValue = Value.newBuilder();
    JsonFormat.parser().merge(instance, instanceValue);

    Value.Builder parameterValueBuilder = Value.newBuilder();
    JsonFormat.parser().merge(PARAMETERS, parameterValueBuilder);
    Value parameterValue = parameterValueBuilder.build();

    List<Value> instances = new ArrayList<>();
    instances.add(instanceValue.build());

    PredictResponse mockResponse = PredictResponse.newBuilder()
        .addPredictions(Value.newBuilder().setStringValue(RESPONSE).build())
        .build();

    Mockito.when(client.predict(GPU_ENDPOINT_ID, instances, parameterValue)).thenReturn(mockResponse);
    // NullPointerException
    Gemma2PredictGpu gemma2PredictGpu = new Gemma2PredictGpu(client);

    String output = gemma2PredictGpu.gemma2PredictGpu(PROJECT_ID, GPU_ENDPOINT_REGION, GPU_ENDPOINT_ID, PARAMETERS);

    assertTrue(output.contains("Rayleigh scattering"));
    verify(client, times(1)).predict(GPU_ENDPOINT_ID, instances, parameterValue);
    assertTrue(instances.contains("inputs"));
    assertTrue(parameterValue.getStructValue().containsFields("temperature"));
    assertTrue(parameterValue.getStructValue().containsFields("maxOutputTokens"));
    assertTrue(parameterValue.getStructValue().containsFields("topP"));
    assertTrue(parameterValue.getStructValue().containsFields("topK"));
  }

  @Test
  public void testShouldRunInterferenceWithTPU() throws IOException {

    String instance = "{ \"prompt\": \"Why is the sky blue?\"}";
    Value.Builder instanceValue = Value.newBuilder();
    JsonFormat.parser().merge(instance, instanceValue);

    Value.Builder parameterValueBuilder = Value.newBuilder();
    JsonFormat.parser().merge(PARAMETERS, parameterValueBuilder);
    Value parameterValue = parameterValueBuilder.build();

    List<Value> instances = new ArrayList<>();
    instances.add(instanceValue.build());

    PredictResponse mockResponse = PredictResponse.newBuilder()
        .addPredictions(Value.newBuilder().setStringValue(RESPONSE).build())
        .build();
    when(client.predict(TPU_ENDPOINT_ID, instances, parameterValue)).thenReturn(mockResponse);
    // NullPointerException
    Gemma2PredictTpu gemma2PredictTpu = new Gemma2PredictTpu(client);
    String output = gemma2PredictTpu.gemma2PredictTpu(PROJECT_ID,TPU_ENDPOINT_REGION, TPU_ENDPOINT_ID, PARAMETERS);

    assertTrue(output.contains("Rayleigh scattering"));
    verify(client, times(1)).predict(GPU_ENDPOINT_ID, instances, parameterValue);
    assertTrue(instances.contains("prompt"));
    assertTrue(parameterValue.getStructValue().containsFields("temperature"));
    assertTrue(parameterValue.getStructValue().containsFields("maxOutputTokens"));
    assertTrue(parameterValue.getStructValue().containsFields("topP"));
    assertTrue(parameterValue.getStructValue().containsFields("topK"));
  }
}
