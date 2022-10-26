/*
 * Copyright 2022 Google LLC
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

package functions;

// [START functions_ocr_save]

import com.google.cloud.functions.CloudEventsFunction;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import functions.eventpojos.MessagePublishedData;
import io.cloudevents.CloudEvent;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.logging.Logger;

public class OcrSaveResult implements CloudEventsFunction {
  // TODO<developer> set this environment variable
  private static final String RESULT_BUCKET = System.getenv("RESULT_BUCKET");

  private static final Storage STORAGE = StorageOptions.getDefaultInstance().getService();
  private static final Logger logger = Logger.getLogger(OcrSaveResult.class.getName());

  // Configure Gson with custom deserializer to handle timestamps in event data
  class DateDeserializer implements JsonDeserializer<OffsetDateTime> {
    @Override
    public OffsetDateTime deserialize(
        JsonElement json, Type typeOfT, JsonDeserializationContext context)
        throws JsonParseException {
      return OffsetDateTime.parse(json.getAsString());
    }
  }

  Gson gson =
      new GsonBuilder().registerTypeAdapter(OffsetDateTime.class, new DateDeserializer()).create();

  @Override
  public void accept(CloudEvent event) {
    // Unmarshal data from CloudEvent
    MessagePublishedData data =
        gson.fromJson(
            new String(event.getData().toBytes(), StandardCharsets.UTF_8),
            MessagePublishedData.class);
    OcrTranslateApiMessage ocrMessage =
        OcrTranslateApiMessage.fromPubsubData(
            data.getMessage().getData().getBytes(StandardCharsets.UTF_8));

    logger.info("Received request to save file " + ocrMessage.getFilename());

    String newFileName =
        String.format("%s_to_%s.txt", ocrMessage.getFilename(), ocrMessage.getLang());

    // Save file to RESULT_BUCKET with name newFileName
    logger.info(String.format("Saving result to %s in bucket %s", newFileName, RESULT_BUCKET));
    BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(RESULT_BUCKET, newFileName)).build();
    STORAGE.create(blobInfo, ocrMessage.getText().getBytes(StandardCharsets.UTF_8));
    logger.info("File saved");
  }
}
// [END functions_ocr_save]
