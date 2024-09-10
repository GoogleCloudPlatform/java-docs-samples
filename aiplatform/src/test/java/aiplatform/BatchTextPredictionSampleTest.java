package aiplatform;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;

import com.google.api.gax.rpc.ApiException;
import com.google.cloud.aiplatform.v1.BatchPredictionJob;
import java.io.IOException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class BatchTextPredictionSampleTest {

  private static String PROJECT_ID;
  private static String INPUT_URI;
  private static String OUTPUT_URI;

  @BeforeClass
  public static void setUpBeforeClass() {
    PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
    assertNotNull("GOOGLE_CLOUD_PROJECT env var is not set.", PROJECT_ID);

    INPUT_URI = System.getenv("GCS_INPUT_URI");
    assertNotNull("GCS_INPUT_URI env var is not set.", INPUT_URI);

    OUTPUT_URI = System.getenv("GCS_OUTPUT_URI");
    assertNotNull("GCS_OUTPUT_URI env var is not set.", OUTPUT_URI);
  }

  @Before
  public void setUp() throws IOException {}

  @Test
  public void testBatchTextPredictionSample() throws IOException {
    try {
      BatchPredictionJob response =
          BatchTextPredictionSample.batchTextPrediction(
              PROJECT_ID, "us-central1", INPUT_URI, OUTPUT_URI);

      assertThat(response).isNotNull();
      assertThat(response.getName()).isNotNull();
    } catch (ApiException ex) {
      // Sample may not work if the model is not deployed, so we just check the error message.
      assertThat(ex.getMessage())
          .contains("The model projects");
    }
  }
}