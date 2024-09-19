package vertexai.gemini;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.aiplatform.v1.EndpointName;
import com.google.cloud.aiplatform.v1.PredictRequest;
import com.google.cloud.aiplatform.v1.PredictResponse;
import com.google.cloud.aiplatform.v1.PredictionServiceClient;
import com.google.cloud.aiplatform.v1.PredictionServiceSettings;
import com.google.protobuf.Value;
import com.google.protobuf.util.JsonFormat;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class Gemma2PredictGpu {

  public static void main(String[] args) throws IOException {
    if (args.length != 2) {
      System.out.println(
          "Usage: java Gemma2PredictGpu <GEMMA2_ENDPOINT_REGION> <GEMMA2_ENDPOINT_ID>");
      System.exit(1);
    }

    String endpointRegion = args[0];
    String endpointId = args[1];
    gemma2PredictGpu(endpointRegion, endpointId);
  }

  public static String gemma2PredictGpu(String endpointRegion, String endpointId)
      throws IOException {
    // TODO(developer): Update & uncomment line below
    // String projectId = "your-project-id";
    String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");

    // Default configuration
    Map<String, Object> config = new HashMap<>();
    config.put("max_tokens", 1024);
    config.put("temperature", 0.9);
    config.put("top_p", 1.0);
    config.put("top_k", 1);

    // Prompt used in the prediction
    String prompt = "Why is the sky blue?";

    // Encapsulate the prompt in a correct format for GPUs
    // Example format: [{'inputs': 'Why is the sky blue?', 'parameters': {'temperature': 0.9}}]
    Map<String, Object> input = new HashMap<>();
    input.put("inputs", prompt);
    input.put("parameters", config);

    // Convert input message to a list of GAPIC instances for model input
    Value.Builder valueBuilder = Value.newBuilder();
    JsonFormat.parser().merge(JsonFormat.printer().print(input), valueBuilder);
    Value instance = valueBuilder.build();

    // Create a client
    GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
    PredictionServiceSettings predictionServiceSettings =
        PredictionServiceSettings.newBuilder()
            .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
            .setEndpoint(String.format("%s-aiplatform.googleapis.com:443", endpointRegion))
            .build();

    try (PredictionServiceClient predictionServiceClient =
             PredictionServiceClient.create(predictionServiceSettings)) {
      // Call the Gemma2 endpoint
      String endpointName =
          EndpointName.of(projectId, endpointRegion, endpointId).toString();
      PredictRequest predictRequest =
          PredictRequest.newBuilder()
              .setEndpoint(endpointName)
              .addAllInstances(Collections.singletonList(instance))
              .build();
      PredictResponse predictResponse = predictionServiceClient.predict(predictRequest);
      String textResponse = predictResponse.getPredictions(0).getStringValue();
      System.out.println(textResponse);
      return textResponse;
    }
  }
}