package snippets;

// [START googlegenaisdk_textgen_with_video]

import com.google.genai.Client;
import com.google.genai.types.Content;
import com.google.genai.types.GenerateContentResponse;
import com.google.genai.types.HttpOptions;
import com.google.genai.types.Part;

public class GenerateContentWithVideo {

  public static void main(String[] args) {
    // TODO(developer): Replace these variables before running the sample.
    String modelId = "gemini-2.0-flash";
    String prompt = " Analyze the provided video file, including its audio.\n"
        + "    Summarize the main points of the video concisely.\n"
        + "    Create a chapter breakdown with timestamps for key sections or topics discussed.";
    generateContent(modelId, prompt);
  }

  public static String generateContent(String modelId, String prompt) {
    // Initialize client that will be used to send requests. This client only needs to be created
    // once, and can be reused for multiple requests.
    try (Client client = Client.builder()
        .httpOptions(HttpOptions.builder().apiVersion("v1").build())
        .build()) {

      GenerateContentResponse response =
          client.models.generateContent(modelId, Content.fromParts(
                  Part.fromText(prompt),
                  Part.fromUri("gs://cloud-samples-data/generative-ai/video/pixel8.mp4", "video/mp4")),
              null);

      System.out.print(response.text());
      // Example response:
      // Here's a breakdown of the video:
      //
      // **Summary:**
      //
      // Saeka Shimada, a photographer in Tokyo, uses the Google Pixel 8 Pro's "Video Boost" feature
      // to ...
      //
      // **Chapter Breakdown with Timestamps:**
      //
      // * **[00:00-00:12] Introduction & Tokyo at Night:** Saeka Shimada introduces herself ...
      return response.text();
    }
  }
}
// [END googlegenaisdk_textgen_with_video]
