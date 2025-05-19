package snippets;

// [START googlegenaisdk_textgen_with_txt_img]

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;

public class GenerateContentWithTextAndImage {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.0-flash";
    generateContent(modelId);
  }

  public static String generateContent(String modelId) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client = Client.builder()
        .httpOptions(HttpOptions.builder().apiVersion("v1").build())
        .build()) {

      GenerateContentResponse response =
          client.models.generateContent(modelId, Content.fromParts(
                  Part.fromText("What is shown in this image?"),
                  Part.fromUri("gs://cloud-samples-data/generative-ai/image/scones.jpg", "image/jpeg")),
              null);

      System.out.print(response.text());
      // Example response:
      // The image shows a flat lay of blueberry scones arranged on parchment paper. There are ...
      return response.text();
    }
  }
}
// [END googlegenaisdk_textgen_with_txt_img]
