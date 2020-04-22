package functions;

// [START functions_pubsub_integration_test]

import static com.google.common.truth.Truth.assertThat;

import io.github.resilience4j.core.IntervalFunction;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.vavr.CheckedRunnable;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.UUID;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

public class ExampleIntegrationTest {
  // Root URL pointing to the locally hosted function
  // The Functions Framework Maven plugin lets us run a function locally
  private static final String BASE_URL = "http://localhost:8080";

  private static Process emulatorProcess = null;
  private static HttpClient client = HttpClientBuilder.create().build();

  @BeforeClass
  public static void setUp() throws IOException {
    // Emulate the function locally by running the Functions Framework Maven plugin
    emulatorProcess = (new ProcessBuilder()).command("mvn", "function:run").start();
  }

  @AfterClass
  public static void tearDown() {
    // Terminate the running Functions Framework Maven plugin process (if it's still running)
    if (emulatorProcess.isAlive()) {
      emulatorProcess.destroy();
    }
  }

  @Test
  public void helloPubSub_shouldRunWithFunctionsFramework() throws Throwable {
    String functionUrl = BASE_URL + "/helloPubsub"; // URL to your locally-running function

    // Initialize constants
    String name = UUID.randomUUID().toString();
    String nameBase64 = Base64.getEncoder().encodeToString(name.getBytes(StandardCharsets.UTF_8));

    String expected = String.format("Hello %s!", name);
    String jsonStr = String.format("{'data': {'data': '%s'}}", nameBase64);

    HttpPost postRequest =  new HttpPost(URI.create(functionUrl));
    postRequest.setEntity(new StringEntity(jsonStr));

    // The Functions Framework Maven plugin process takes time to start up
    // Use resilience4j to retry the test HTTP request until the plugin responds
    RetryRegistry registry = RetryRegistry.of(RetryConfig.custom()
        .maxAttempts(8)
        .retryExceptions(HttpHostConnectException.class)
        .intervalFunction(IntervalFunction.ofExponentialBackoff(200, 2))
        .build());
    Retry retry = registry.retry("my");

    // Perform the request-retry process
    CheckedRunnable retriableFunc = Retry.decorateCheckedRunnable(
        retry, () -> client.execute(postRequest));
    retriableFunc.run();

    // Get Functions Framework plugin process' stdout
    InputStream stdoutStream = emulatorProcess.getErrorStream();
    ByteArrayOutputStream stdoutBytes = new ByteArrayOutputStream();
    stdoutBytes.write(stdoutStream.readNBytes(stdoutStream.available()));

    // Verify desired name value is present
    assertThat(stdoutBytes.toString(StandardCharsets.UTF_8)).contains(expected);
  }
}
// [END functions_pubsub_integration_test]

