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

import java.io.IOException;
import org.json.JSONObject;
import recaptcha.CreateAssessment.AssessmentResponse;

public class Recaptcha {

  public static JSONObject execute(String projectId, JSONObject jsonObject) {
    double sampleThresholdScore = 0.50;
    String verdict = "";

    String recaptchaSiteKey = jsonObject.getJSONObject("recaptcha_cred").getString("sitekey");
    String recaptchaAction = jsonObject.getJSONObject("recaptcha_cred").getString("action");
    String recaptchaToken = jsonObject.getJSONObject("recaptcha_cred").getString("token");

    try {
      AssessmentResponse response = new CreateAssessment().createAssessment(projectId, recaptchaSiteKey, recaptchaToken, recaptchaAction);

      if (response == null) {
        return new JSONObject()
            .put("error_msg", "Something happened! Please try again")
            .put("success", "false");
      }

      if (response.recaptchaScore < sampleThresholdScore) {
        verdict = "Not a human";
      } else {
        verdict = "human";
      }

      JSONObject result = new JSONObject()
          .put("score", response.recaptchaScore)
          .put("verdict", verdict);

      return new JSONObject().put("data", result).put("success", "true");

    } catch (IOException e) {
      return new JSONObject()
          .put("error_msg", "Something happened! Please try again")
          .put("success", "false");
    }

  }

}
