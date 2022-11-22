/*
 * Copyright 2020 Google LLC
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

import com.google.cloud.functions.BackgroundFunction;
import com.google.cloud.functions.Context;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import functions.eventpojos.PubSubMessage;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

public class OcrSaveResult implements BackgroundFunction<PubSubMessage> {
  // TODO<developer> set this environment variable
  private static final String RESULT_BUCKET = System.getenv("RESULT_BUCKET");

  private static final Storage STORAGE = StorageOptions.getDefaultInstance().getService();
  private static final Logger logger = Logger.getLogger(OcrSaveResult.class.getName());

  @Override
  public void accept(PubSubMessage pubSubMessage, Context context) {
    OcrTranslateApiMessage ocrMessage = OcrTranslateApiMessage.fromPubsubData(
        pubSubMessage.getData().getBytes(StandardCharsets.UTF_8));

    logger.info("Received request to save file " +  ocrMessage.getFilename());

    String newFileName = String.format(
        "%s_to_%s.txt", ocrMessage.getFilename(), ocrMessage.getLang());

    // Save file to RESULT_BUCKET with name newFileNaem
    logger.info(String.format("Saving result to %s in bucket %s", newFileName, RESULT_BUCKET));
    BlobInfo blobInfo = BlobInfo.newBuilder(BlobId.of(RESULT_BUCKET, newFileName)).build();
    STORAGE.create(blobInfo, ocrMessage.getText().getBytes(StandardCharsets.UTF_8));
    logger.info("File saved");
  }
}
// [END functions_ocr_save]
