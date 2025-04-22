/*
 * Copyright 2025 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package modelarmor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.cloud.modelarmor.v1.ModelArmorClient;
import com.google.cloud.modelarmor.v1.ModelArmorSettings;
import com.google.cloud.modelarmor.v1.SdpAdvancedConfig;
import com.google.cloud.modelarmor.v1.Template;
import com.google.cloud.modelarmor.v1.TemplateName;
import com.google.privacy.dlp.v2.CreateDeidentifyTemplateRequest;
import com.google.privacy.dlp.v2.CreateInspectTemplateRequest;
import com.google.privacy.dlp.v2.DeidentifyConfig;
import com.google.privacy.dlp.v2.DeidentifyTemplate;
import com.google.privacy.dlp.v2.DeidentifyTemplateName;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InfoTypeTransformations;
import com.google.privacy.dlp.v2.InfoTypeTransformations.InfoTypeTransformation;
import com.google.privacy.dlp.v2.InspectConfig;
import com.google.privacy.dlp.v2.InspectTemplate;
import com.google.privacy.dlp.v2.InspectTemplateName;
import com.google.privacy.dlp.v2.LocationName;
import com.google.privacy.dlp.v2.PrimitiveTransformation;
import com.google.privacy.dlp.v2.ReplaceValueConfig;
import com.google.privacy.dlp.v2.Value;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Integration (system) tests for {@link Snippets}. */
@RunWith(JUnit4.class)
public class SnippetsIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String LOCATION_ID = System.getenv()
      .getOrDefault("GOOGLE_CLOUD_PROJECT_LOCATION", "us-central1");
  private static final String MA_ENDPOINT =
      String.format("modelarmor.%s.rep.googleapis.com:443", LOCATION_ID);
  private static String TEST_TEMPLATE_ID;
  private static String TEST_INSPECT_TEMPLATE_ID;
  private static String TEST_DEIDENTIFY_TEMPLATE_ID;
  private static String TEST_TEMPLATE_NAME;
  private static String TEST_INSPECT_TEMPLATE_NAME;
  private static String TEST_DEIDENTIFY_TEMPLATE_NAME;
  private ByteArrayOutputStream stdOut;

  // Check if the required environment variables are set.
  private static String requireEnvVar(String varName) {
    String value = System.getenv(varName);
    assertNotNull("Environment variable " + varName + " is required to run these tests.",
        System.getenv(varName));
    return value;
  }

  @BeforeClass
  public static void beforeAll() throws IOException {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");
  
    TEST_TEMPLATE_ID = randomId();
    TEST_INSPECT_TEMPLATE_ID = randomId();
    TEST_DEIDENTIFY_TEMPLATE_ID = randomId();
    TEST_TEMPLATE_NAME = TemplateName.of(PROJECT_ID, LOCATION_ID, TEST_TEMPLATE_ID).toString();
    TEST_INSPECT_TEMPLATE_NAME = InspectTemplateName
        .ofProjectLocationInspectTemplateName(PROJECT_ID, LOCATION_ID, TEST_INSPECT_TEMPLATE_ID)
        .toString();
    TEST_DEIDENTIFY_TEMPLATE_NAME = DeidentifyTemplateName.ofProjectLocationDeidentifyTemplateName(
        PROJECT_ID, LOCATION_ID, TEST_DEIDENTIFY_TEMPLATE_ID).toString();

    createInspectTemplate(TEST_INSPECT_TEMPLATE_ID);
    createDeidentifyTemplate(TEST_DEIDENTIFY_TEMPLATE_ID);
  }

  @AfterClass
  public static void afterAll() throws IOException {
    requireEnvVar("GOOGLE_CLOUD_PROJECT");

    try {
      deleteModelArmorTemplate(TEST_TEMPLATE_ID);
    } catch (NotFoundException e) {
      // Ignore not found error - template already deleted.
    }
    deleteSdpTemplates();
  }

  @Before
  public void beforeEach() {
    stdOut = new ByteArrayOutputStream();
    System.setOut(new PrintStream(stdOut));
  }

  @After
  public void afterEach() throws IOException {
    stdOut = null;
    System.setOut(null);
  }

  private static String randomId() {
    Random random = new Random();
    return "java-ma-" + random.nextLong();
  }

  private static void deleteModelArmorTemplate(String templateId) throws IOException {
    ModelArmorSettings modelArmorSettings = ModelArmorSettings.newBuilder().setEndpoint(MA_ENDPOINT)
        .build();

    try (ModelArmorClient client = ModelArmorClient.create(modelArmorSettings)) {
      String name = TemplateName.of(PROJECT_ID, LOCATION_ID, templateId).toString();
      client.deleteTemplate(name);
    }
  }

  private static void deleteSdpTemplates() throws IOException {
    try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {
      dlpServiceClient.deleteInspectTemplate(TEST_INSPECT_TEMPLATE_NAME);
      dlpServiceClient.deleteDeidentifyTemplate(TEST_DEIDENTIFY_TEMPLATE_NAME);
    }
  }

  private static InspectTemplate createInspectTemplate(String templateId) throws IOException {
    try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {
      // Info Types: https://cloud.google.com/sensitive-data-protection/docs/infotypes-reference
      List<InfoType> infoTypes = Stream
          .of("PHONE_NUMBER", "EMAIL_ADDRESS", "US_INDIVIDUAL_TAXPAYER_IDENTIFICATION_NUMBER")
          .map(it -> InfoType.newBuilder().setName(it).build())
          .collect(Collectors.toList());

      InspectConfig inspectConfig = InspectConfig.newBuilder()
          .addAllInfoTypes(infoTypes)
          .build();

      InspectTemplate inspectTemplate = InspectTemplate.newBuilder()
          .setInspectConfig(inspectConfig)
          .build();

      CreateInspectTemplateRequest createInspectTemplateRequest = 
          CreateInspectTemplateRequest.newBuilder()
            .setParent(LocationName.of(PROJECT_ID, LOCATION_ID).toString())
            .setTemplateId(templateId)
            .setInspectTemplate(inspectTemplate)
            .build();

      return dlpServiceClient.createInspectTemplate(createInspectTemplateRequest);
    }
  }

  private static DeidentifyTemplate createDeidentifyTemplate(String templateId) throws IOException {
    try (DlpServiceClient dlpServiceClient = DlpServiceClient.create()) {
      // Specify replacement string to be used for the finding.
      ReplaceValueConfig replaceValueConfig = ReplaceValueConfig.newBuilder()
          .setNewValue(Value.newBuilder().setStringValue("[REDACTED]").build())
          .build();

      // Define type of deidentification.
      PrimitiveTransformation primitiveTransformation = PrimitiveTransformation.newBuilder()
          .setReplaceConfig(replaceValueConfig)
          .build();

      // Associate deidentification type with info type.
      InfoTypeTransformation transformation = InfoTypeTransformation.newBuilder()
          .setPrimitiveTransformation(primitiveTransformation)
          .build();

      // Construct the configuration for the Redact request and list all desired transformations.
      DeidentifyConfig redactConfig = DeidentifyConfig.newBuilder()
          .setInfoTypeTransformations(
            InfoTypeTransformations.newBuilder()
            .addTransformations(transformation))
          .build();

      DeidentifyTemplate deidentifyTemplate = DeidentifyTemplate.newBuilder()
          .setDeidentifyConfig(redactConfig)
          .build();

      CreateDeidentifyTemplateRequest createDeidentifyTemplateRequest = 
          CreateDeidentifyTemplateRequest.newBuilder()
            .setParent(LocationName.of(PROJECT_ID, LOCATION_ID).toString())
            .setTemplateId(templateId)
            .setDeidentifyTemplate(deidentifyTemplate)
            .build();

      return dlpServiceClient.createDeidentifyTemplate(createDeidentifyTemplateRequest);
    }
  }

  @Test
  public void testCreateModelArmorTemplateWithAdvancedSDP() throws IOException {

    Template createdTemplate = CreateTemplateWithAdvancedSdp.createTemplateWithAdvancedSdp(
        PROJECT_ID, LOCATION_ID, TEST_TEMPLATE_ID,
        TEST_INSPECT_TEMPLATE_ID, TEST_DEIDENTIFY_TEMPLATE_ID);

    assertEquals(TEST_TEMPLATE_NAME, createdTemplate.getName());

    SdpAdvancedConfig advancedSdpConfig = createdTemplate.getFilterConfig().getSdpSettings()
        .getAdvancedConfig();

    assertEquals(TEST_INSPECT_TEMPLATE_NAME, advancedSdpConfig.getInspectTemplate());
    assertEquals(TEST_DEIDENTIFY_TEMPLATE_NAME, advancedSdpConfig.getDeidentifyTemplate());
  }
}
