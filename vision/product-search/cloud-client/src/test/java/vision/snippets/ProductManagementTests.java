package vision.snippets;

import com.example.vision.ProductManagement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;

import static com.google.common.truth.Truth.assertThat;

@RunWith(JUnit4.class)
public class ProductManagementTests {

  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String COMPUTE_REGION = "us-west1";
  private static final String PRODUCT_DISPLAY_NAME = "fake_prod_display_name_for_testing";
  private static final String PRODUCT_CATEGORY = "homegoods";
  private static final String PRODUCT_ID = "fake_prod_id_for_testing";
  private ByteArrayOutputStream bout;
  private PrintStream out;

  @Before
  public void setUp() throws IOException {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
  }

  @After
  public void tearDown() throws IOException {
    ProductManagement.deleteProduct(PROJECT_ID, COMPUTE_REGION, PRODUCT_ID);
    System.setOut(null);
  }

  @Test
  public void testPurgeOrphanProducts() throws Exception {
    // Act
    ProductManagement.createProduct(
            PROJECT_ID, COMPUTE_REGION, PRODUCT_ID, PRODUCT_DISPLAY_NAME, PRODUCT_CATEGORY);
    ProductManagement.listProducts(PROJECT_ID, COMPUTE_REGION);

    // Assert
    String got = bout.toString();
    assertThat(got).contains(PRODUCT_ID);

    bout.reset();

    // Act
    PurgeProducts.purgeOrphanProducts(PROJECT_ID, COMPUTE_REGION, true);

    // Assert
    got = bout.toString();
    ProductManagement.listProducts(PROJECT_ID, COMPUTE_REGION);
    assertThat(got).doesNotContain(PRODUCT_ID);
  }
}
