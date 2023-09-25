/*
 * Copyright 2023 Google LLC
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

package recaptcha;

import com.google.protobuf.ByteString;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class Util {

  public static String substring(String line) {
    return line.substring((line.lastIndexOf(":") + 1)).trim();
  }

  public static String getAssessmentName(String stdout) {
    String assessmentName = "";
    for (String line : stdout.split("\n")) {
      if (line.contains("Assessment name: ")) {
        assessmentName = Util.substring(line);
        break;
      }
    }
    return assessmentName;
  }

  public static ByteString createHashedAccountId()
      throws NoSuchAlgorithmException, InvalidKeyException {
    String userIdentifier = "Alice Bob";
    // Change this to a secret not shared with Google.
    final String HMAC_KEY = "123456789";
    // Get instance of Mac object implementing HmacSHA256, and initialize it with the above
    // secret key.
    Mac mac = Mac.getInstance("HmacSHA256");
    mac.init(new SecretKeySpec(HMAC_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
    byte[] hashBytes = mac.doFinal(userIdentifier.getBytes(StandardCharsets.UTF_8));
    return ByteString.copyFrom(hashBytes);
  }

}