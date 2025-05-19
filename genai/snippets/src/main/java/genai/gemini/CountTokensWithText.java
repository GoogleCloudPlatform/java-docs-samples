package snippets;

// [START googlegenaisdk_counttoken_with_txt]

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.CountTokensResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;
import java.util.List;
import java.util.Optional;

public class CountTokensWithText {

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
              Part.fromText("What's the highest mountain in Africa?")))
          .build();

      CountTokensResponse response =
          client.models.countTokens(modelId, List.of(content), null);

      System.out.print(response);
      // Example response:
      // CountTokensResponse{totalTokens=Optional[9], cachedContentTokenCount=Optional.empty}
      return response.totalTokens();
    }
  }
}
// [END googlegenaisdk_counttoken_with_txt]



