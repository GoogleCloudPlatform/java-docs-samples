package vertexai.gemini.samples;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.generativeai.preview.ChatSession;
import com.google.cloud.vertexai.generativeai.preview.GenerativeModel;
import com.google.cloud.vertexai.generativeai.preview.ResponseHandler;
import com.google.cloud.vertexai.v1beta1.GenerateContentResponse;
import com.google.cloud.vertexai.v1beta1.GenerationConfig;

public class ChatDiscussion {

    private static final String PROJECT_ID = "glaforge-genai-playground";
    private static final String LOCATION = "us-central1";
    private static final String MODEL_NAME = "gemini-pro-vision";

    public static void main(String[] args) throws Exception {
        try (VertexAI vertexAI = new VertexAI(PROJECT_ID, LOCATION)) {
            GenerativeModel model = new GenerativeModel(
                MODEL_NAME,
                GenerationConfig.newBuilder().setMaxOutputTokens(512).build(),
                vertexAI
            );

            ChatSession chatSession = new ChatSession(model);

            GenerateContentResponse response;

            System.out.println("===================");
            response = chatSession.sendMessage("What are large language models?");
            System.out.println(ResponseHandler.getText(response));

            System.out.println("===================");
            response = chatSession.sendMessage("How do they work?");
            System.out.println(ResponseHandler.getText(response));

            System.out.println("===================");
            response = chatSession.sendMessage("Can you please name some of them?");
            System.out.println(ResponseHandler.getText(response));
        }
    }
}