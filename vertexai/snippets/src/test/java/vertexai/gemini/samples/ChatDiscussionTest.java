package vertexai.gemini.samples;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.IOException;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

@RunWith(JUnit4.class)
public class ChatDiscussionTest {
    private static final String GCP_PROJECT_VAR_NAME = "GOOGLE_CLOUD_PROJECT";

    @BeforeClass
    public static void setUp() {
        assertWithMessage(String.format("Missing environment variable '%s' ", GCP_PROJECT_VAR_NAME))
            .that(System.getenv(GCP_PROJECT_VAR_NAME))
            .isNotEmpty();
    }

    @Test
    public void checkChatDiscussion() throws IOException {
        String output = ChatDiscussion.threeQuestions();
        System.out.println(output);

        assertThat(output).isNotEmpty();
        assertThat(output).contains("LLM");
        assertThat(output).contains("model");
    }
}
