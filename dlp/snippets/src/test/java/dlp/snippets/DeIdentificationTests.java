/*
 * Copyright 2019 Google LLC
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

package dlp.snippets;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth.assertWithMessage;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.cloud.dlp.v2.DlpServiceClient;
import com.google.common.collect.ImmutableList;
import com.google.privacy.dlp.v2.CreateDlpJobRequest;
import com.google.privacy.dlp.v2.DlpJob;
import com.google.privacy.dlp.v2.FieldId;
import com.google.privacy.dlp.v2.GetDlpJobRequest;
import com.google.privacy.dlp.v2.InfoType;
import com.google.privacy.dlp.v2.InfoTypeStats;
import com.google.privacy.dlp.v2.InspectDataSourceDetails;
import com.google.privacy.dlp.v2.Table;
import com.google.privacy.dlp.v2.Table.Row;
import com.google.privacy.dlp.v2.Value;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

@RunWith(JUnit4.class)
public class DeIdentificationTests extends TestBase {

  @Override
  protected ImmutableList<String> requiredEnvVars() {
    return ImmutableList.of(
        "GOOGLE_APPLICATION_CREDENTIALS",
        "GOOGLE_CLOUD_PROJECT",
        "DLP_DEID_WRAPPED_KEY",
        "DLP_DEID_KEY_NAME");
  }

  @Test
  public void testDeIdentifyWithMasking() throws IOException {
    DeIdentifyWithMasking.deIdentifyWithMasking(PROJECT_ID, "My SSN is 372819127");

    String output = bout.toString();
    assertThat(output).contains("Text after masking:");
  }

  @Test
  public void testDeIdentifyWithFpe() throws IOException {
    DeIdentifyWithFpe.deIdentifyWithFpe(
        PROJECT_ID, "My SSN is 372819127", KMS_KEY_NAME, WRAPPED_KEY);

    String output = bout.toString();
    assertThat(output).contains("Text after format-preserving encryption:");
  }

  @Test
  public void testReIdentifyWithFpe() throws IOException {
    ReIdentifyWithFpe.reIdentifyWithFpe(
        PROJECT_ID, "My SSN is SSN_TOKEN(9):731997681", KMS_KEY_NAME, WRAPPED_KEY);

    String output = bout.toString();
    assertThat(output).contains("Text after re-identification:");
  }

  @Test
  public void testDeIdentifyTextWithFpe() throws IOException {
    DeIdentifyTextWithFpe.deIdentifyTextWithFpe(
        PROJECT_ID, "My phone number is 4359916732", KMS_KEY_NAME, WRAPPED_KEY);

    String output = bout.toString();
    assertThat(output).contains("Text after format-preserving encryption: ");
  }

  @Test
  public void testReIdentifyTextWithFpe() throws IOException {
    ReIdentifyTextWithFpe.reIdentifyTextWithFpe(
        PROJECT_ID, "My phone number is PHONE_TOKEN(10):9617256398", KMS_KEY_NAME, WRAPPED_KEY);

    String output = bout.toString();
    assertThat(output).contains("Text after re-identification: ");
  }

  @Test
  public void testDeIdentifyTableWithFpe() throws IOException {
    Table tableToDeIdentify =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("Employee ID").build())
            .addHeaders(FieldId.newBuilder().setName("Date").build())
            .addHeaders(FieldId.newBuilder().setName("Compensation").build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("11111").build())
                    .addValues(Value.newBuilder().setStringValue("2015").build())
                    .addValues(Value.newBuilder().setStringValue("$10").build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("22222").build())
                    .addValues(Value.newBuilder().setStringValue("2016").build())
                    .addValues(Value.newBuilder().setStringValue("$20").build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("33333").build())
                    .addValues(Value.newBuilder().setStringValue("2016").build())
                    .addValues(Value.newBuilder().setStringValue("$15").build())
                    .build())
            .build();

    DeIdentifyTableWithFpe.deIdentifyTableWithFpe(
        PROJECT_ID, tableToDeIdentify, KMS_KEY_NAME, WRAPPED_KEY);

    String output = bout.toString();
    assertThat(output).contains("Table after format-preserving encryption:");
  }

  @Test
  public void testReIdentifyTableWithFpe() throws IOException {
    Table tableToReIdentify =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("Employee ID").build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("28777").build())
                    .build())
            .build();

    ReIdentifyTableWithFpe.reIdentifyTableWithFpe(
        PROJECT_ID, tableToReIdentify, KMS_KEY_NAME, WRAPPED_KEY);

    String output = bout.toString();
    assertThat(output).contains("Table after re-identification:");
  }

  @Test
  public void testDeIdentifyTableBucketing() throws IOException {
    // Transform a column based on the value of another column
    Table tableToDeIdentify =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("AGE").build())
            .addHeaders(FieldId.newBuilder().setName("PATIENT").build())
            .addHeaders(FieldId.newBuilder().setName("HAPPINESS SCORE").build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("101").build())
                    .addValues(Value.newBuilder().setStringValue("Charles Dickens").build())
                    .addValues(Value.newBuilder().setStringValue("95").build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("22").build())
                    .addValues(Value.newBuilder().setStringValue("Jane Austen").build())
                    .addValues(Value.newBuilder().setStringValue("21").build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("55").build())
                    .addValues(Value.newBuilder().setStringValue("Mark Twain").build())
                    .addValues(Value.newBuilder().setStringValue("75").build())
                    .build())
            .build();
    Table expectedTable =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("AGE").build())
            .addHeaders(FieldId.newBuilder().setName("PATIENT").build())
            .addHeaders(FieldId.newBuilder().setName("HAPPINESS SCORE").build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("101").build())
                    .addValues(Value.newBuilder().setStringValue("Charles Dickens").build())
                    .addValues(Value.newBuilder().setStringValue("90:100").build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("22").build())
                    .addValues(Value.newBuilder().setStringValue("Jane Austen").build())
                    .addValues(Value.newBuilder().setStringValue("20:30").build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("55").build())
                    .addValues(Value.newBuilder().setStringValue("Mark Twain").build())
                    .addValues(Value.newBuilder().setStringValue("70:80").build())
                    .build())
            .build();

    Table table = DeIdentifyTableBucketing.deIdentifyTableBucketing(PROJECT_ID, tableToDeIdentify);

    String output = bout.toString();
    assertThat(output).contains("Table after de-identification:");
    assertThat(table).isEqualTo(expectedTable);
  }

  @Test
  public void testDeIdentifyTableConditionMasking() throws IOException {
    // Transform a column based on the value of another column
    Table tableToDeIdentify =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("AGE").build())
            .addHeaders(FieldId.newBuilder().setName("PATIENT").build())
            .addHeaders(FieldId.newBuilder().setName("HAPPINESS SCORE").build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("101").build())
                    .addValues(Value.newBuilder().setStringValue("Charles Dickens").build())
                    .addValues(Value.newBuilder().setStringValue("95").build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("22").build())
                    .addValues(Value.newBuilder().setStringValue("Jane Austen").build())
                    .addValues(Value.newBuilder().setStringValue("21").build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("55").build())
                    .addValues(Value.newBuilder().setStringValue("Mark Twain").build())
                    .addValues(Value.newBuilder().setStringValue("75").build())
                    .build())
            .build();
    Table expectedTable =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("AGE").build())
            .addHeaders(FieldId.newBuilder().setName("PATIENT").build())
            .addHeaders(FieldId.newBuilder().setName("HAPPINESS SCORE").build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("101").build())
                    .addValues(Value.newBuilder().setStringValue("Charles Dickens").build())
                    .addValues(Value.newBuilder().setStringValue("**").build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("22").build())
                    .addValues(Value.newBuilder().setStringValue("Jane Austen").build())
                    .addValues(Value.newBuilder().setStringValue("21").build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("55").build())
                    .addValues(Value.newBuilder().setStringValue("Mark Twain").build())
                    .addValues(Value.newBuilder().setStringValue("75").build())
                    .build())
            .build();

    Table table =
        DeIdentifyTableConditionMasking.deIdentifyTableConditionMasking(
            PROJECT_ID, tableToDeIdentify);

    String output = bout.toString();
    assertThat(output).contains("Table after de-identification:");
    assertThat(table).isEqualTo(expectedTable);
  }

  @Test
  public void testDeIdentifyTableInfoTypes() throws IOException {
    // Transform findings found in column
    Table tableToDeIdentify =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("AGE").build())
            .addHeaders(FieldId.newBuilder().setName("PATIENT").build())
            .addHeaders(FieldId.newBuilder().setName("HAPPINESS SCORE").build())
            .addHeaders(FieldId.newBuilder().setName("FACTOID").build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("101").build())
                    .addValues(Value.newBuilder().setStringValue("Charles Dickens").build())
                    .addValues(Value.newBuilder().setStringValue("95").build())
                    .addValues(
                        Value.newBuilder()
                            .setStringValue(
                                "Charles Dickens name was a curse invented by Shakespeare.")
                            .build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("22").build())
                    .addValues(Value.newBuilder().setStringValue("Jane Austen").build())
                    .addValues(Value.newBuilder().setStringValue("21").build())
                    .addValues(
                        Value.newBuilder()
                            .setStringValue("There are 14 kisses in Jane Austen's novels.")
                            .build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("55").build())
                    .addValues(Value.newBuilder().setStringValue("Mark Twain").build())
                    .addValues(Value.newBuilder().setStringValue("75").build())
                    .addValues(Value.newBuilder().setStringValue("Mark Twain loved cats.").build())
                    .build())
            .build();
    Table expectedTable =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("AGE").build())
            .addHeaders(FieldId.newBuilder().setName("PATIENT").build())
            .addHeaders(FieldId.newBuilder().setName("HAPPINESS SCORE").build())
            .addHeaders(FieldId.newBuilder().setName("FACTOID").build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("101").build())
                    .addValues(Value.newBuilder().setStringValue("[PERSON_NAME]").build())
                    .addValues(Value.newBuilder().setStringValue("95").build())
                    .addValues(
                        Value.newBuilder()
                            .setStringValue(
                                "[PERSON_NAME] name was a curse invented by [PERSON_NAME].")
                            .build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("22").build())
                    .addValues(Value.newBuilder().setStringValue("[PERSON_NAME]").build())
                    .addValues(Value.newBuilder().setStringValue("21").build())
                    .addValues(
                        Value.newBuilder()
                            .setStringValue("There are 14 kisses in [PERSON_NAME] novels.")
                            .build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("55").build())
                    .addValues(Value.newBuilder().setStringValue("[PERSON_NAME]").build())
                    .addValues(Value.newBuilder().setStringValue("75").build())
                    .addValues(
                        Value.newBuilder().setStringValue("[PERSON_NAME] loved cats.").build())
                    .build())
            .build();

    Table table = DeIdentifyTableInfoTypes.deIdentifyTableInfoTypes(PROJECT_ID, tableToDeIdentify);

    String output = bout.toString();
    assertThat(output).contains("Table after de-identification:");
    assertThat(table).isEqualTo(expectedTable);
  }

  @Test
  public void testDeIdentifyTableRowSuppress() throws IOException {
    // Suppress a row based on the content of a column
    Table tableToDeIdentify =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("AGE").build())
            .addHeaders(FieldId.newBuilder().setName("PATIENT").build())
            .addHeaders(FieldId.newBuilder().setName("HAPPINESS SCORE").build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("101").build())
                    .addValues(Value.newBuilder().setStringValue("Charles Dickens").build())
                    .addValues(Value.newBuilder().setStringValue("95").build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("22").build())
                    .addValues(Value.newBuilder().setStringValue("Jane Austen").build())
                    .addValues(Value.newBuilder().setStringValue("21").build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("55").build())
                    .addValues(Value.newBuilder().setStringValue("Mark Twain").build())
                    .addValues(Value.newBuilder().setStringValue("75").build())
                    .build())
            .build();
    Table expectedTable =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("AGE").build())
            .addHeaders(FieldId.newBuilder().setName("PATIENT").build())
            .addHeaders(FieldId.newBuilder().setName("HAPPINESS SCORE").build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("22").build())
                    .addValues(Value.newBuilder().setStringValue("Jane Austen").build())
                    .addValues(Value.newBuilder().setStringValue("21").build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("55").build())
                    .addValues(Value.newBuilder().setStringValue("Mark Twain").build())
                    .addValues(Value.newBuilder().setStringValue("75").build())
                    .build())
            .build();

    Table table =
        DeIdentifyTableRowSuppress.deIdentifyTableRowSuppress(PROJECT_ID, tableToDeIdentify);

    String output = bout.toString();
    assertThat(output).contains("Table after de-identification:");
    assertThat(table).isEqualTo(expectedTable);
  }

  @Test
  public void testDeIdentifyTableConditionsInfoTypes() throws IOException {
    // Transform findings only when specific conditions are met on another field
    Table tableToDeIdentify =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("AGE").build())
            .addHeaders(FieldId.newBuilder().setName("PATIENT").build())
            .addHeaders(FieldId.newBuilder().setName("HAPPINESS SCORE").build())
            .addHeaders(FieldId.newBuilder().setName("FACTOID").build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("101").build())
                    .addValues(Value.newBuilder().setStringValue("Charles Dickens").build())
                    .addValues(Value.newBuilder().setStringValue("95").build())
                    .addValues(
                        Value.newBuilder()
                            .setStringValue(
                                "Charles Dickens name was a curse invented by Shakespeare.")
                            .build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("22").build())
                    .addValues(Value.newBuilder().setStringValue("Jane Austen").build())
                    .addValues(Value.newBuilder().setStringValue("21").build())
                    .addValues(
                        Value.newBuilder()
                            .setStringValue("There are 14 kisses in Jane Austen's novels.")
                            .build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("55").build())
                    .addValues(Value.newBuilder().setStringValue("Mark Twain").build())
                    .addValues(Value.newBuilder().setStringValue("75").build())
                    .addValues(Value.newBuilder().setStringValue("Mark Twain loved cats.").build())
                    .build())
            .build();
    Table expectedTable =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("AGE").build())
            .addHeaders(FieldId.newBuilder().setName("PATIENT").build())
            .addHeaders(FieldId.newBuilder().setName("HAPPINESS SCORE").build())
            .addHeaders(FieldId.newBuilder().setName("FACTOID").build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("101").build())
                    .addValues(Value.newBuilder().setStringValue("[PERSON_NAME]").build())
                    .addValues(Value.newBuilder().setStringValue("95").build())
                    .addValues(
                        Value.newBuilder()
                            .setStringValue(
                                "[PERSON_NAME] name was a curse invented by [PERSON_NAME].")
                            .build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("22").build())
                    .addValues(Value.newBuilder().setStringValue("Jane Austen").build())
                    .addValues(Value.newBuilder().setStringValue("21").build())
                    .addValues(
                        Value.newBuilder()
                            .setStringValue("There are 14 kisses in Jane Austen's novels.")
                            .build())
                    .build())
            .addRows(
                Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("55").build())
                    .addValues(Value.newBuilder().setStringValue("Mark Twain").build())
                    .addValues(Value.newBuilder().setStringValue("75").build())
                    .addValues(Value.newBuilder().setStringValue("Mark Twain loved cats.").build())
                    .build())
            .build();

    Table table =
        DeIdentifyTableConditionInfoTypes.deIdentifyTableConditionInfoTypes(
            PROJECT_ID, tableToDeIdentify);

    String output = bout.toString();
    assertThat(output).contains("Table after de-identification:");
    assertThat(table).isEqualTo(expectedTable);
  }

  @Test
  public void testDeIdentifyWithDateShift() throws IOException {
    Path inputFile = Paths.get("src/test/resources/dates.csv");
    assertWithMessage("Input file must exist").that(inputFile.toFile().exists()).isTrue();
    Path outputFile = Paths.get("src/test/resources/results.csv");
    assertWithMessage("Output file must be writeable").that(inputFile.toFile().canWrite()).isTrue();
    DeIdentifyWithDateShift.deIdentifyWithDateShift(PROJECT_ID, inputFile, outputFile);

    String output = bout.toString();
    assertThat(output).contains("Content written to file: ");

    // Clean up test output
    Files.delete(outputFile);
  }

  @Test
  public void testDeIdentifyWithRedaction() throws IOException {
    DeIdentifyWithRedaction.deIdentifyWithRedaction(
        PROJECT_ID, "My name is Alicia Abernathy, and my email address is aabernathy@example.com.");

    String output = bout.toString();
    assertThat(output)
        .contains(
            "Text after redaction: " + "My name is Alicia Abernathy, and my email address is .");
  }

  @Test
  public void testDeIdentifyWithReplacement() throws IOException {
    DeIdentifyWithReplacement.deIdentifyWithReplacement(
        PROJECT_ID, "My name is Alicia Abernathy, and my email address is aabernathy@example.com.");

    String output = bout.toString();
    assertThat(output)
        .contains(
            "Text after redaction: "
                + "My name is Alicia Abernathy, and my email address is [email-address].");
  }

  @Test
  public void testDeIdentifyWithInfoType() throws IOException {
    DeIdentifyWithInfoType.deIdentifyWithInfoType(PROJECT_ID, "My email is test@example.com");

    String output = bout.toString();
    assertThat(output).contains("Text after redaction: " + "My email is [EMAIL_ADDRESS]");
  }

  @Test
  public void testDeIdentifyWithSimpleWordList() throws IOException {
    DeIdentifyWithSimpleWordList.deidentifyWithSimpleWordList(
        PROJECT_ID, "Patient was seen in RM-YELLOW then transferred to rm green.");

    String output = bout.toString();
    assertThat(output).contains("Text after replace with infotype config: ");
  }

  @Test
  public void testDeIdentifyWithExceptionList() throws IOException {
    DeIdentifyWithExceptionList.deIdentifyWithExceptionList(
        PROJECT_ID, "jack@example.org accessed customer record of user5@example.com");

    String output = bout.toString();
    assertThat(output).contains("Text after replace with infotype config: ");
  }

  @Test
  public void testDeIdentifyWithDeterministicEncryption() throws IOException {
    DeIdenitfyWithDeterministicEncryption.deIdentifyWithDeterministicEncryption(
        PROJECT_ID, "My SSN is 372819127", WRAPPED_KEY, KMS_KEY_NAME);
    String output = bout.toString();
    assertThat(output).contains("Text after de-identification:");
  }

  @Test
  public void testReIdentifyWithDeterministicEncryption() throws IOException {
    String textToReIdentify =
        DeIdenitfyWithDeterministicEncryption.deIdentifyWithDeterministicEncryption(
            PROJECT_ID, "My SSN is 372819127", WRAPPED_KEY, KMS_KEY_NAME);
    ReidentifyWithDeterministicEncryption.reIdentifyWithDeterminsiticEncryption(
        PROJECT_ID, textToReIdentify, WRAPPED_KEY, KMS_KEY_NAME);
    String output = bout.toString();
    assertThat(output).contains("Text after re-identification: My SSN is 372819127");
  }

  @Test
  public void testDeIdentifyWithFpeSurrogate() throws IOException, NoSuchAlgorithmException {

    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
    keyGenerator.init(128);
    SecretKey secretKey = keyGenerator.generateKey();

    // Convert key to Base64 encoded string
    byte[] keyBytes = secretKey.getEncoded();
    String unwrappedKey = Base64.getEncoder().encodeToString(keyBytes);


    DeidentifyFreeTextWithFpeUsingSurrogate.deIdentifyWithFpeSurrogate(
        PROJECT_ID, "My phone number is 4359916732", unwrappedKey);
    String output = bout.toString();
    assertThat(output).contains("Text after de-identification: ");
  }

  @Test
  public void testDeIdentifyWithTimeExtraction() throws IOException {
    Table tableToDeIdentify =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("Name").build())
            .addHeaders(FieldId.newBuilder().setName("Birth Date").build())
            .addHeaders(FieldId.newBuilder().setName("Credit Card").build())
            .addHeaders(FieldId.newBuilder().setName("Register Date").build())
            .addRows(
                Table.Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("Alex").build())
                    .addValues(Value.newBuilder().setStringValue("01/01/1970").build())
                    .addValues(Value.newBuilder().setStringValue("4532908762519852").build())
                    .addValues(Value.newBuilder().setStringValue("07/21/1996").build())
                    .build())
            .addRows(
                Table.Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("Charlie").build())
                    .addValues(Value.newBuilder().setStringValue("03/06/1988").build())
                    .addValues(Value.newBuilder().setStringValue("4301261899725540").build())
                    .addValues(Value.newBuilder().setStringValue("04/09/2001").build())
                    .build())
            .build();
    Table expectedTable =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("Name").build())
            .addHeaders(FieldId.newBuilder().setName("Birth Date").build())
            .addHeaders(FieldId.newBuilder().setName("Credit Card").build())
            .addHeaders(FieldId.newBuilder().setName("Register Date").build())
            .addRows(
                Table.Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("Alex").build())
                    .addValues(Value.newBuilder().setStringValue("1970").build())
                    .addValues(Value.newBuilder().setStringValue("4532908762519852").build())
                    .addValues(Value.newBuilder().setStringValue("1996").build())
                    .build())
            .addRows(
                Table.Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("Charlie").build())
                    .addValues(Value.newBuilder().setStringValue("1988").build())
                    .addValues(Value.newBuilder().setStringValue("4301261899725540").build())
                    .addValues(Value.newBuilder().setStringValue("2001").build())
                    .build())
            .build();
    Table table =
        DeIdentifyWithTimeExtraction.deIdentifyWithTimeExtraction(PROJECT_ID, tableToDeIdentify);
    String output = bout.toString();
    assertThat(output).contains("Table after de-identification:");
    assertThat(table).isEqualTo(expectedTable);
  }

  @Test
  public void testDeIdentifyDataReplaceWithDictionary() throws IOException {
    DeIdentifyDataReplaceWithDictionary.deidentifyDataReplaceWithDictionary(
        PROJECT_ID, "My name is Charlie and email address is charlie@example.com.");
    String output = bout.toString();
    assertThat(
            ImmutableList.of(
                "Text after de-identification: My name is Charlie "
                        + "and email address is izumi@example.com.",
                "Text after de-identification: My name is Charlie "
                        + "and email address is alex@example.com."))
        .contains(output);
  }

  @Test
  public void testReIdentifyWithFpeSurrogate() throws IOException, NoSuchAlgorithmException {

    KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
    keyGenerator.init(128);
    SecretKey secretKey = keyGenerator.generateKey();
    byte[] keyBytes = secretKey.getEncoded();

    String unwrappedKey = Base64.getEncoder().encodeToString(keyBytes);
    String textToDeIdentify = "My phone number is 4359916731";

    String textToReIdentify =
        DeidentifyFreeTextWithFpeUsingSurrogate.deIdentifyWithFpeSurrogate(
            PROJECT_ID, textToDeIdentify, unwrappedKey);

    ReidentifyFreeTextWithFpeUsingSurrogate.reIdentifyWithFpeSurrogate(
        PROJECT_ID, textToReIdentify, unwrappedKey);

    String output = bout.toString();
    assertThat(output).contains("Text after re-identification: ");
  }

  @Test
  public void testDeIdentifyWithBucketingConfig() throws IOException {

    Table tableToDeIdentify =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("AGE").build())
            .addHeaders(FieldId.newBuilder().setName("PATIENT").build())
            .addHeaders(FieldId.newBuilder().setName("HAPPINESS SCORE").build())
            .addRows(
                Table.Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("101").build())
                    .addValues(Value.newBuilder().setStringValue("Charles Dickens").build())
                    .addValues(Value.newBuilder().setIntegerValue(95).build())
                    .build())
            .addRows(
                Table.Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("22").build())
                    .addValues(Value.newBuilder().setStringValue("Jane Austen").build())
                    .addValues(Value.newBuilder().setIntegerValue(21).build())
                    .build())
            .addRows(
                Table.Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("55").build())
                    .addValues(Value.newBuilder().setStringValue("Mark Twain").build())
                    .addValues(Value.newBuilder().setIntegerValue(75).build())
                    .build())
            .build();

    Table expectedTable =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("AGE").build())
            .addHeaders(FieldId.newBuilder().setName("PATIENT").build())
            .addHeaders(FieldId.newBuilder().setName("HAPPINESS SCORE").build())
            .addRows(
                Table.Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("101").build())
                    .addValues(Value.newBuilder().setStringValue("Charles Dickens").build())
                    .addValues(Value.newBuilder().setStringValue("High").build())
                    .build())
            .addRows(
                Table.Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("22").build())
                    .addValues(Value.newBuilder().setStringValue("Jane Austen").build())
                    .addValues(Value.newBuilder().setStringValue("low").build())
                    .build())
            .addRows(
                Table.Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("55").build())
                    .addValues(Value.newBuilder().setStringValue("Mark Twain").build())
                    .addValues(Value.newBuilder().setStringValue("High").build())
                    .build())
            .build();

    Table actualTable =
        DeIdentifyTableWithBucketingConfig.deIdentifyTableBucketing(PROJECT_ID, tableToDeIdentify);
    String output = bout.toString();
    assertThat(actualTable).isEqualTo(expectedTable);
    assertThat(output).contains("Table after de-identification: ");
  }

  @Test
  public void testDeIdentifyTableWithMultipleCryptoHash() throws IOException {

    Table tableToDeIdentify =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("userid").build())
            .addHeaders(FieldId.newBuilder().setName("comments").build())
            .addRows(
                Table.Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("user1@example.org").build())
                    .addValues(
                        Value.newBuilder()
                            .setStringValue(
                                "my email is user1@example.org and phone is 858-555-0222")
                            .build())
                    .build())
            .build();

    // Transient keys are generated by DLP API for each request and used for hashing the data.
    String transientKeyName1 = "TransientKeyName1";
    String transientKeyName2 = "TransientKeyName2";

    DeIdentifyTableWithMultipleCryptoHash.deIdentifyWithCryptHashTransformation(
        PROJECT_ID, tableToDeIdentify, transientKeyName1, transientKeyName2);
    String output = bout.toString();
    assertThat(output).contains("Table after de-identification: ");
    assertThat(output).doesNotContain("user1@example.org");
    assertThat(output).doesNotContain("858-555-0222");
  }

  @Test
  public void testDeIdentifyTableWithCryptoHash() throws IOException {

    Table tableToDeIdentify =
        Table.newBuilder()
            .addHeaders(FieldId.newBuilder().setName("userid").build())
            .addHeaders(FieldId.newBuilder().setName("comments").build())
            .addRows(
                Table.Row.newBuilder()
                    .addValues(Value.newBuilder().setStringValue("user1@example.org").build())
                    .addValues(
                        Value.newBuilder()
                            .setStringValue(
                                "my email is user1@example.org and phone is 858-555-0222")
                            .build())
                    .build())
            .build();

    // Transient key is generated by DLP API for each request and used for hashing the data.
    String transientKeyName = "TransientKeyName";

    DeIdentifyTableWithCryptoHash.deIdentifyWithCryptHashTransformation(
        PROJECT_ID, tableToDeIdentify, transientKeyName);
    String output = bout.toString();
    assertThat(output).contains("Table after de-identification: ");
    assertThat(output).doesNotContain("user1@example.org");
    assertThat(output).doesNotContain("858-555-0222");
  }

  @Test
  public void testDeIdentifyStorage() throws IOException, InterruptedException {
    DlpServiceClient dlpServiceClient = mock(DlpServiceClient.class);

    try (MockedStatic<DlpServiceClient> mockedStatic = Mockito.mockStatic(DlpServiceClient.class)) {
      mockedStatic.when(() -> DlpServiceClient.create()).thenReturn(dlpServiceClient);

      InfoTypeStats infoTypeStats =
          InfoTypeStats.newBuilder()
              .setInfoType(InfoType.newBuilder().setName("EMAIL_ADDRESS").build())
              .setCount(2)
              .build();
      DlpJob dlpJob =
          DlpJob.newBuilder()
              .setName("projects/project_id/locations/global/dlpJobs/job_id")
              .setState(DlpJob.JobState.DONE)
              .setInspectDetails(
                  InspectDataSourceDetails.newBuilder()
                      .setResult(
                          InspectDataSourceDetails.Result.newBuilder()
                              .addInfoTypeStats(infoTypeStats)
                              .build()))
              .build();
      when(dlpServiceClient.createDlpJob(any(CreateDlpJobRequest.class)))
          .thenReturn(dlpJob);
      when(dlpServiceClient.getDlpJob((GetDlpJobRequest) any())).thenReturn(dlpJob);

      DeidentifyCloudStorage.deidentifyCloudStorage(
          "project_id",
          "gs://input_bucket/test.txt",
          "table_id",
          "dataset_id",
          "gs://output_bucket",
          "deidentify_template_id",
          "deidentify_structured_template_id",
          "image_redact_template_id");
      String output = bout.toString();
      assertThat(output).contains("Job status: DONE");
      assertThat(output).contains("Job name: projects/project_id/locations/global/dlpJobs/job_id");
      assertThat(output).contains("Info type: EMAIL_ADDRESS");
      assertThat(output).contains("Count: 2");
    }
  }
}
