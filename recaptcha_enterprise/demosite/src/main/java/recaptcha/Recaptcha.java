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

package recaptcha;

import org.json.JSONObject;
import recaptcha.CreateAssessment.AssessmentResponse;

public class Recaptcha {

  // Wrapper method for create assessment calls.
  public static JSONObject executeCreateAssessment(String projectId, JSONObject jsonObject) {
    double sampleThresholdScore = 0.50;
    String verdict = "";

    String recaptchaSiteKey = jsonObject.getJSONObject("recaptcha_cred").getString("sitekey");
    String recaptchaAction = jsonObject.getJSONObject("recaptcha_cred").getString("action");
    String recaptchaToken = jsonObject.getJSONObject("recaptcha_cred").getString("token");

    try {
      AssessmentResponse response = CreateAssessment.createAssessment(projectId, recaptchaSiteKey,
          recaptchaAction, recaptchaToken);

      if (response.recaptchaScore < sampleThresholdScore) {
        verdict = "Not a human";
      } else {
        verdict = "Human";
      }

      JSONObject result = new JSONObject()
          .put("score", response.recaptchaScore)
          .put("verdict", verdict);

      return new JSONObject().put("data", result).put("success", "true");

    } catch (Exception | Error e) {
      return new JSONObject()
          .put("error_msg", e)
          .put("success", "false");
    }
  }
}
