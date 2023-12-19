package vertexai.gemini;

// [START aiplatform_gemini_token_count]
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.CountTokensResponse;
import com.google.cloud.vertexai.generativeai.preview.GenerativeModel;

import java.io.IOException;

public class GetTokenCount {
    public static void main(String[] args) throws IOException {
        // TODO(developer): Replace these variables before running the sample.
        String projectId = "your-google-cloud-project-id";
        String location = "us-central1";
        String modelName = "gemini-pro-vision";

        String textPrompt = "How many tokens are there in this prompt?";
        getTokenCount(projectId, location, modelName, textPrompt);
    }

    public static int getTokenCount(String projectId, String location, String modelName,
                                    String textPrompt)
        throws IOException {
        try (VertexAI vertexAI = new VertexAI(projectId, location)) {
            GenerativeModel model = new GenerativeModel(modelName, vertexAI);
            CountTokensResponse response = model.countTokens(textPrompt);

            int tokenCount = response.getTotalTokens();
            System.out.println("There are " + tokenCount + " tokens in the prompt.");

            return tokenCount;
        }
    }
}
// [END aiplatform_gemini_token_count]