package snippets;

// [START googlegenaisdk_textgen_sys_instr_with_txt]

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;

public class GenerateContentWithSystemInstruction {

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

      GenerateContentConfig config = GenerateContentConfig.builder()
          .systemInstruction(Content.fromParts(
              Part.fromText("You're a language translator."),
              Part.fromText("Your mission is to translate text in English to French.")))
          .build();

      GenerateContentResponse response =
          client.models.generateContent(modelId, Content.fromParts(
                  Part.fromText("Why is the sky blue?")),
              config);

      System.out.print(response.text());
      // Example response:
      // Pourquoi le ciel est-il bleu ?
      return response.text();
    }
  }
}
// [END googlegenaisdk_textgen_sys_instr_with_txt]


