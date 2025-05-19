package snippets;

// [START googlegenaisdk_textgen_with_txt_stream]

import com.google.genai.Client;
import com.google.genai.ResponseStream;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;

public class GenerateContentStream {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String contents = "Why is the sky blue?";
    String modelId = "gemini-2.0-flash";
    generateContent(modelId, contents);
  }

  public static String generateContent(String modelId, String contents) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client = Client.builder()
        .httpOptions(HttpOptions.builder().apiVersion("v1").build())
        .build()) {

      StringBuilder responseTextBuilder = new StringBuilder();
      ResponseStream<GenerateContentResponse> responseStream =
          client.models.generateContentStream(modelId, contents, null);

      for (GenerateContentResponse chunk : responseStream) {
        System.out.print(chunk.text());
        responseTextBuilder.append(chunk.text());
      }
      // Example response:
      // The sky appears blue due to a phenomenon called **Rayleigh scattering**. Here's
      // a breakdown of why:
      // ...
      return responseTextBuilder.toString();
    }
  }
}
// [END googlegenaisdk_textgen_with_txt_stream]