package snippets;

// [START googlegenaisdk_ctrlgen_with_enum_schema]

import com.google.genai.Client;
import com.google.genai.types.GenerateContentConfig;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Schema;
import com.google.genai.types.Type;
import java.util.List;

public class GenerateContentWithEnumSchema {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String contents = "What type of instrument is an oboe?";
    String modelId = "gemini-2.0-flash";
    generateContent(modelId, contents);
  }

  public static String generateContent(String modelId, String contents) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client = Client.builder()
        .httpOptions(HttpOptions.builder().apiVersion("v1").build())
        .build()) {

      // Define the response schema with an enum.
      Schema responseSchema =
          Schema.builder()
              .type(Type.Known.STRING)
              .enum_(
                  List.of("Percussion", "String", "Woodwind", "Brass", "Keyboard"))
              .build();

      GenerateContentConfig config = GenerateContentConfig.builder()
          .responseMimeType("text/x.enum")
          .responseSchema(responseSchema)
          .build();

      GenerateContentResponse response =
          client.models.generateContent(modelId, contents, config);

      System.out.print(response.text());
      // Example response:
      // Woodwind
      return response.text();
    }
  }
}
// [END googlegenaisdk_ctrlgen_with_enum_schema]
