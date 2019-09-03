package vision.snippets;

import com.example.vision.ProductInProductSetManagement;
import com.example.vision.ProductManagement;
import com.example.vision.ProductSetManagement;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.UUID;

import static com.google.common.truth.Truth.assertThat;

public class ProductInProductSetManagementTests {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String COMPUTE_REGION = "us-west1";
  private static final String PRODUCT_SET_DISPLAY_NAME =
          "fake_pdt_set_display_name_for_testing";
  private static final String PRODUCT_SET_ID = "fake_pdt_set_id_for_testing" + UUID.randomUUID();
  private static final String PRODUCT_DISPLAY_NAME = "fake_pdt_display_name_for_testing";
  private static final String PRODUCT_CATEGORY = "apparel";
  private static final String PRODUCT_ID = "fake_pdt_id_for_testing";
  private ByteArrayOutputStream bout;
  private PrintStream out;

  @Before
  public void setUp() throws IOException {
    bout = new ByteArrayOutputStream();
    out = new PrintStream(bout);
    System.setOut(out);
    ProductSetManagement.createProductSet(
            PROJECT_ID, COMPUTE_REGION, PRODUCT_SET_ID, PRODUCT_SET_DISPLAY_NAME);
    ProductManagement.createProduct(
            PROJECT_ID, COMPUTE_REGION, PRODUCT_ID, PRODUCT_DISPLAY_NAME, PRODUCT_CATEGORY);
    bout.reset();
  }

  @After
  public void tearDown() throws IOException {
    ProductManagement.deleteProduct(PROJECT_ID, COMPUTE_REGION, PRODUCT_ID);
    ProductSetManagement.deleteProductSet(PROJECT_ID, COMPUTE_REGION, PRODUCT_SET_ID);
    System.setOut(null);
  }

  @Test
  public void testPurgeProductsInProductSet() throws Exception {
    // Act
    ProductInProductSetManagement.addProductToProductSet(
            PROJECT_ID, COMPUTE_REGION, PRODUCT_ID, PRODUCT_SET_ID);
    ProductManagement.listProducts(
            PROJECT_ID, COMPUTE_REGION);

    // Assert
    String got = bout.toString();
    assertThat(got).contains(PRODUCT_ID);

    bout.reset();
    PurgeProductsInProductSet.purgeProductsInProductSet(
            PROJECT_ID, COMPUTE_REGION, PRODUCT_SET_ID, true);

    ProductManagement.listProducts(
            PROJECT_ID, COMPUTE_REGION);

    // Assert
    got = bout.toString();
    assertThat(got).doesNotContain(PRODUCT_ID);
  }
}
