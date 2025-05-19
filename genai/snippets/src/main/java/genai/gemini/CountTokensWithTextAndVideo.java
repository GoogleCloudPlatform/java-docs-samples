package snippets;

// [START googlegenaisdk_counttoken_with_txt_vid]

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.CountTokensResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;
import java.util.List;
import java.util.Optional;

public class CountTokensWithTextAndVideo {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.0-flash";
    countTokens(modelId);
  }

  public static Optional<Integer> countTokens(String modelId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client = Client.builder()
        .httpOptions(HttpOptions.builder().apiVersion("v1").build())
        .build()) {

      Content content = Content.builder()
          .parts(List.of(
              Part.fromText("Provide a description of this video"),
              Part.fromUri("gs://cloud-samples-data/generative-ai/video/pixel8.mp4", "video/mp4")))
          .build();

      CountTokensResponse response =
          client.models.countTokens(modelId, List.of(content),
              null);

      System.out.print(response);
      // Example response:
      // CountTokensResponse{totalTokens=Optional[16251], cachedContentTokenCount=Optional.empty}
      return response.totalTokens();
    }
  }
}
// [END googlegenaisdk_counttoken_with_txt_vid]


