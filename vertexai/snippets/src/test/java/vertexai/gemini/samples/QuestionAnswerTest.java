package vertexai.gemini.samples;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;

@RunWith(JUnit4.class)
public class QuestionAnswerTest {
    private static final String GCP_PROJECT_VAR_NAME = "GOOGLE_CLOUD_PROJECT";

    @BeforeClass
    public static void setUp() {
        assertWithMessage(String.format("Missing environment variable '%s' ", GCP_PROJECT_VAR_NAME))
            .that(System.getenv(GCP_PROJECT_VAR_NAME))
            .isNotEmpty();
    }

    @Test
    public void simpleQuestion() throws Exception {
        String output = QuestionAnswer.simpleQuestion();
        System.out.println(output);

        assertThat(output).isNotEmpty();
        assertThat(output).contains("Rayleigh scattering");
    }
}
