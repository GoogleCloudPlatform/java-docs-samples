/*
 * Copyright 2026 Google LLC
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

package parametermanager;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.api.client.util.Strings;
import com.google.api.gax.rpc.InvalidArgumentException;
import com.google.api.gax.rpc.NotFoundException;
import com.google.cloud.parametermanager.v1.LocationName;
import com.google.cloud.parametermanager.v1.Parameter;
import com.google.cloud.parametermanager.v1.ParameterFormat;
import com.google.cloud.parametermanager.v1.ParameterManagerClient;
import com.google.cloud.parametermanager.v1.ParameterName;
import com.google.cloud.parametermanager.v1.ParameterVersion;
import com.google.cloud.parametermanager.v1.ParameterVersionName;
import com.google.cloud.parametermanager.v1.ParameterVersionPayload;
import com.google.cloud.parametermanager.v1.RenderTemplateVersionResponse;
import com.google.cloud.parametermanager.v1.Template;
import com.google.cloud.parametermanager.v1.TemplateFormat;
import com.google.cloud.parametermanager.v1.TemplateName;
import com.google.cloud.parametermanager.v1.TemplateVersion;
import com.google.cloud.parametermanager.v1.TemplateVersionName;
import com.google.cloud.parametermanager.v1.TemplateVersionPayload;
import com.google.cloud.resourcemanager.v3.TagBinding;
import com.google.cloud.resourcemanager.v3.TagBindingsClient;
import com.google.protobuf.ByteString;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.zip.CRC32C;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TemplatesTagsChecksumIT {
  private static final String PROJECT_ID = System.getenv("GOOGLE_CLOUD_PROJECT");
  private static final String TAG_KEY = System.getenv("PARAMETER_MANAGER_TAG_KEY");
  private static final String TAG_VALUE = System.getenv("PARAMETER_MANAGER_TAG_VALUE");
  private static final String TEMPLATE_PAYLOAD =
      "{\"username\": \"{{.username}}\", \"host\": \"{{.host}}\"}";
  private static final String VALUES_PAYLOAD =
      "{\"username\": \"test-user\", \"host\": \"localhost\"}";
  private static final String VERSION_ID = "v1";

  private static final List<String> TEMPLATE_VERSIONS = new ArrayList<>();
  private static final List<String> TEMPLATES = new ArrayList<>();
  private static final List<String> PARAMETER_VERSIONS = new ArrayList<>();
  private static final List<String> PARAMETERS = new ArrayList<>();

  @BeforeClass
  public static void beforeAll() {
    Assert.assertFalse("missing GOOGLE_CLOUD_PROJECT", Strings.isNullOrEmpty(PROJECT_ID));
  }

  @AfterClass
  public static void afterAll() throws IOException {
    try (ParameterManagerClient client = client()) {
      for (String name : TEMPLATE_VERSIONS) {
        try {
          client.deleteTemplateVersion(name);
        } catch (NotFoundException e) {
          // Already deleted by the test.
        }
      }
      for (String name : TEMPLATES) {
        try {
          client.deleteTemplate(name);
        } catch (NotFoundException e) {
          // Already deleted by the test.
        }
      }
      for (String name : PARAMETER_VERSIONS) {
        try {
          client.deleteParameterVersion(name);
        } catch (NotFoundException e) {
          // Already deleted by the test.
        }
      }
      for (String name : PARAMETERS) {
        try {
          client.deleteParameter(name);
        } catch (NotFoundException e) {
          // Already deleted by the test.
        }
      }
    }
  }

  private static ParameterManagerClient client() throws IOException {
    return ParameterManagerClient.create();
  }

  private static TagBindingsClient tagBindingsClient() throws IOException {
    return TagBindingsClient.create();
  }

  private static void assumeTagsConfigured() {
    Assume.assumeFalse(
        "missing PARAMETER_MANAGER_TAG_KEY or PARAMETER_MANAGER_TAG_VALUE",
        Strings.isNullOrEmpty(TAG_KEY) || Strings.isNullOrEmpty(TAG_VALUE));
  }

  private static String randomId(String prefix) {
    return prefix + "-" + UUID.randomUUID().toString().substring(0, 8);
  }

  private static String createTemplate() throws IOException {
    String templateId = randomId("java-template");
    try (ParameterManagerClient client = client()) {
      Template template =
          client.createTemplate(
              LocationName.of(PROJECT_ID, "global").toString(),
              Template.newBuilder().setFormat(TemplateFormat.TEMPLATE_FORMAT_JSON).build(),
              templateId);
      TEMPLATES.add(template.getName());
    }
    return templateId;
  }

  private static void createTemplateVersion(String templateId, String versionId)
      throws IOException {
    try (ParameterManagerClient client = client()) {
      TemplateVersion version =
          client.createTemplateVersion(
              TemplateName.of(PROJECT_ID, "global", templateId).toString(),
              TemplateVersion.newBuilder()
                  .setPayload(
                      TemplateVersionPayload.newBuilder()
                          .setData(ByteString.copyFromUtf8(TEMPLATE_PAYLOAD)))
                  .build(),
              versionId);
      TEMPLATE_VERSIONS.add(version.getName());
    }
  }

  private static String createParameter() throws IOException {
    return createParameter(ParameterFormat.JSON);
  }

  private static String createParameter(ParameterFormat format) throws IOException {
    String parameterId = randomId("java-param");
    try (ParameterManagerClient client = client()) {
      Parameter parameter =
          client.createParameter(
              LocationName.of(PROJECT_ID, "global").toString(),
              Parameter.newBuilder().setFormat(format).build(),
              parameterId);
      PARAMETERS.add(parameter.getName());
    }
    return parameterId;
  }

  private static void createParameterVersion(String parameterId, String versionId, byte[] data)
      throws IOException {
    try (ParameterManagerClient client = client()) {
      CRC32C crc32c = new CRC32C();
      crc32c.update(data);
      ParameterVersion version =
          client.createParameterVersion(
              ParameterName.of(PROJECT_ID, "global", parameterId).toString(),
              ParameterVersion.newBuilder()
                  .setPayload(
                      ParameterVersionPayload.newBuilder()
                          .setData(ByteString.copyFrom(data))
                          .setDataCrc32C(crc32c.getValue()))
                  .build(),
              versionId);
      PARAMETER_VERSIONS.add(version.getName());
    }
  }

  private static List<String> tagValuesOf(String parameterId) throws IOException {
    List<String> tagValues = new ArrayList<>();
    try (ParameterManagerClient client = client();
        TagBindingsClient tagBindingsClient = tagBindingsClient()) {
      Parameter parameter =
          client.getParameter(ParameterName.of(PROJECT_ID, "global", parameterId).toString());
      tagBindingsClient
          .listTagBindings("//parametermanager.googleapis.com/" + parameter.getName())
          .iterateAll()
          .forEach(binding -> tagValues.add(binding.getTagValue()));
    }
    return tagValues;
  }

  @Test
  public void createParamTemplate() throws IOException {
    String templateId = randomId("java-template");
    Template template =
        CreateParamTemplate.createParamTemplate(
            PROJECT_ID, templateId, TemplateFormat.TEMPLATE_FORMAT_JSON);
    TEMPLATES.add(template.getName());
    assertThat(template.getName()).contains(templateId);
    assertThat(template.getFormat()).isEqualTo(TemplateFormat.TEMPLATE_FORMAT_JSON);
  }

  @Test
  public void createParamTemplateVersion() throws IOException {
    String templateId = createTemplate();
    TemplateVersion version =
        CreateParamTemplateVersion.createParamTemplateVersion(PROJECT_ID, templateId, VERSION_ID);
    TEMPLATE_VERSIONS.add(version.getName());
    assertThat(version.getName()).contains(VERSION_ID);
  }

  @Test
  public void listParamTemplates() throws IOException {
    String templateId = createTemplate();
    boolean found = false;
    for (Template template : ListParamTemplates.listParamTemplates(PROJECT_ID).iterateAll()) {
      found |= template.getName().contains(templateId);
    }
    assertThat(found).isTrue();
  }

  @Test
  public void getParamTemplate() throws IOException {
    String templateId = createTemplate();
    Template template = GetParamTemplate.getParamTemplate(PROJECT_ID, templateId);
    assertThat(template.getName()).contains(templateId);
  }

  @Test
  public void listParamTemplateVersions() throws IOException {
    String templateId = createTemplate();
    createTemplateVersion(templateId, VERSION_ID);
    boolean found = false;
    for (TemplateVersion version :
        ListParamTemplateVersions.listParamTemplateVersions(PROJECT_ID, templateId).iterateAll()) {
      found |= version.getName().contains(VERSION_ID);
    }
    assertThat(found).isTrue();
  }

  @Test
  public void getParamTemplateVersion() throws IOException {
    String templateId = createTemplate();
    createTemplateVersion(templateId, VERSION_ID);
    TemplateVersion version =
        GetParamTemplateVersion.getParamTemplateVersion(PROJECT_ID, templateId, VERSION_ID);
    assertThat(version.getPayload().getData().toStringUtf8()).isEqualTo(TEMPLATE_PAYLOAD);
  }

  @Test
  public void updateParamTemplateLabels() throws IOException {
    String templateId = createTemplate();
    Template template =
        UpdateParamTemplateLabels.updateParamTemplateLabels(
            PROJECT_ID, templateId, "environment", "test");
    assertThat(template.getLabelsMap()).containsEntry("environment", "test");
  }

  @Test
  public void deleteParamTemplate() throws IOException {
    String templateId = createTemplate();
    DeleteParamTemplate.deleteParamTemplate(PROJECT_ID, templateId);
    try (ParameterManagerClient client = client()) {
      assertThrows(
          NotFoundException.class,
          () -> client.getTemplate(TemplateName.of(PROJECT_ID, "global", templateId).toString()));
    }
  }

  @Test
  public void disableParamTemplateVersion() throws IOException {
    String templateId = createTemplate();
    createTemplateVersion(templateId, VERSION_ID);
    TemplateVersion version =
        DisableParamTemplateVersion.disableParamTemplateVersion(PROJECT_ID, templateId, VERSION_ID);
    assertThat(version.getDisabled()).isTrue();
  }

  @Test
  public void enableParamTemplateVersion() throws IOException {
    String templateId = createTemplate();
    createTemplateVersion(templateId, VERSION_ID);
    DisableParamTemplateVersion.disableParamTemplateVersion(PROJECT_ID, templateId, VERSION_ID);
    TemplateVersion version =
        EnableParamTemplateVersion.enableParamTemplateVersion(PROJECT_ID, templateId, VERSION_ID);
    assertThat(version.getDisabled()).isFalse();
  }

  @Test
  public void deleteParamTemplateVersion() throws IOException {
    String templateId = createTemplate();
    createTemplateVersion(templateId, VERSION_ID);
    DeleteParamTemplateVersion.deleteParamTemplateVersion(PROJECT_ID, templateId, VERSION_ID);
    try (ParameterManagerClient client = client()) {
      assertThrows(
          NotFoundException.class,
          () ->
              client.getTemplateVersion(
                  TemplateVersionName.of(PROJECT_ID, "global", templateId, VERSION_ID).toString()));
    }
  }

  @Test
  public void renderParamTemplateVersion() throws IOException {
    String templateId = createTemplate();
    createTemplateVersion(templateId, VERSION_ID);
    String parameterId = createParameter();
    createParameterVersion(
        parameterId, VERSION_ID, VALUES_PAYLOAD.getBytes(StandardCharsets.UTF_8));
    RenderTemplateVersionResponse response =
        RenderParamTemplateVersion.renderParamTemplateVersion(
            PROJECT_ID, templateId, VERSION_ID, parameterId, VERSION_ID);
    assertThat(response.getPayload().getData().toStringUtf8()).isEqualTo(TEMPLATE_PAYLOAD);
    assertThat(response.getRenderedPayload().toStringUtf8()).isEqualTo(VALUES_PAYLOAD);
  }

  @Test
  public void templateQuickstart() throws Exception {
    String templateId = randomId("java-template");
    String parameterId = randomId("java-param");
    TEMPLATES.add(TemplateName.of(PROJECT_ID, "global", templateId).toString());
    TEMPLATE_VERSIONS.add(
        TemplateVersionName.of(PROJECT_ID, "global", templateId, VERSION_ID).toString());
    PARAMETERS.add(ParameterName.of(PROJECT_ID, "global", parameterId).toString());
    PARAMETER_VERSIONS.add(
        ParameterVersionName.of(PROJECT_ID, "global", parameterId, VERSION_ID).toString());

    PrintStream originalOut = System.out;
    ByteArrayOutputStream redirected = new ByteArrayOutputStream();
    System.setOut(new PrintStream(redirected));
    try {
      TemplateQuickstart.templateQuickstart(PROJECT_ID, templateId, parameterId, VERSION_ID);
      assertThat(redirected.toString()).contains(VALUES_PAYLOAD);
    } finally {
      System.setOut(originalOut);
    }
  }

  @Test
  public void createParamWithTags() throws IOException {
    assumeTagsConfigured();
    String parameterId = randomId("java-param");
    Parameter parameter =
        CreateParamWithTags.createParamWithTags(PROJECT_ID, parameterId, TAG_KEY, TAG_VALUE);
    PARAMETERS.add(parameter.getName());
    assertThat(tagValuesOf(parameterId)).contains(TAG_VALUE);
  }

  @Test
  public void bindTagsToParam() throws Exception {
    assumeTagsConfigured();
    String parameterId = randomId("java-param");
    PARAMETERS.add(ParameterName.of(PROJECT_ID, "global", parameterId).toString());
    TagBinding binding = BindTagsToParam.bindTagsToParam(PROJECT_ID, parameterId, TAG_VALUE);
    assertThat(binding.getTagValue()).isEqualTo(TAG_VALUE);
    assertThat(tagValuesOf(parameterId)).contains(TAG_VALUE);
  }

  @Test
  public void getParamTags() throws Exception {
    assumeTagsConfigured();
    String parameterId = randomId("java-param");
    PARAMETERS.add(ParameterName.of(PROJECT_ID, "global", parameterId).toString());
    BindTagsToParam.bindTagsToParam(PROJECT_ID, parameterId, TAG_VALUE);
    List<String> tagValues = new ArrayList<>();
    GetParamTags.getParamTags(PROJECT_ID, parameterId)
        .iterateAll()
        .forEach(binding -> tagValues.add(binding.getTagValue()));
    assertThat(tagValues).contains(TAG_VALUE);
  }

  @Test
  public void createParamVersionWithChecksum() throws IOException {
    String parameterId = createParameter(ParameterFormat.UNFORMATTED);
    ParameterVersion version =
        CreateParamVersionWithChecksum.createParamVersionWithChecksum(
            PROJECT_ID, parameterId, VERSION_ID);
    PARAMETER_VERSIONS.add(version.getName());
    CRC32C crc32c = new CRC32C();
    crc32c.update("hello world!".getBytes(StandardCharsets.UTF_8));
    assertThat(version.getChecksumSource())
        .isEqualTo(ParameterVersion.ChecksumSource.USER_SPECIFIED);
    assertThat(version.getPayload().getDataCrc32C()).isEqualTo(crc32c.getValue());
  }

  @Test
  public void getParamVersionVerifyChecksum() throws IOException {
    String parameterId = createParameter(ParameterFormat.UNFORMATTED);
    createParameterVersion(
        parameterId, VERSION_ID, "hello world!".getBytes(StandardCharsets.UTF_8));
    ParameterVersion version =
        GetParamVersionVerifyChecksum.getParamVersionVerifyChecksum(
            PROJECT_ID, parameterId, VERSION_ID);
    assertThat(version.getChecksumSource())
        .isEqualTo(ParameterVersion.ChecksumSource.USER_SPECIFIED);
    assertThat(version.getPayload().getData().toStringUtf8()).isEqualTo("hello world!");
  }

  @Test
  public void createParamVersionWithChecksum_mismatch() throws IOException {
    String parameterId = createParameter(ParameterFormat.UNFORMATTED);
    try (ParameterManagerClient client = client()) {
      ParameterVersion version =
          ParameterVersion.newBuilder()
              .setPayload(
                  ParameterVersionPayload.newBuilder()
                      .setData(ByteString.copyFromUtf8("hello world!"))
                      .setDataCrc32C(1L))
              .build();
      InvalidArgumentException e =
          assertThrows(
              InvalidArgumentException.class,
              () ->
                  client.createParameterVersion(
                      ParameterName.of(PROJECT_ID, "global", parameterId).toString(),
                      version,
                      VERSION_ID));
      assertThat(e.getReason()).isEqualTo("CHECKSUM_MISMATCH");
    }
  }
}
