package tpu;

import com.google.cloud.tpu.v2alpha1.QueuedResource;
import com.google.cloud.tpu.v2alpha1.TpuClient;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;

@RunWith(JUnit4.class)
@Timeout(value = 30)
public class DeleteIT {
  private static final String PROJECT_ID = "project-id";
  private static final String ZONE = "europe-west4-a";
  private static final String NODE_NAME = "test-tpu";
  private ByteArrayOutputStream bout;

  @Before
  public void setUp() {
    bout = new ByteArrayOutputStream();
    System.setOut(new PrintStream(bout));
  }

  @Test
  public void testCreateQueuedResource() {
    try (MockedStatic<TpuClient> mockedTpuClient = mockStatic(TpuClient.class)) {
      QueuedResource mockQueuedResource = mock(QueuedResource.class);
      TpuClient mockTpuClient = mock(TpuClient.class);

    }
  }
}
