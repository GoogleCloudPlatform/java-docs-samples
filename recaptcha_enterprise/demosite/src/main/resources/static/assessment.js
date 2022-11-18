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

function assessRecaptcha(obj, event) {
  event.preventDefault();
  grecaptcha.enterprise.ready(function () {
    grecaptcha.enterprise.execute(obj.getAttribute("data-sitekey"),
        {action: obj.getAttribute("data-action")}).then(function (token) {
      console.log(token);
      httpRequest(token, obj.getAttribute("data-action"),
          obj.getAttribute("data-sitekey"));
    });
  });
}

function httpRequest(token, action, sitekey) {
  $.ajax({
    url: '/create_assessment',
    data: JSON.stringify({
      "recaptcha_cred": {
        "token": token,
        "action": action,
        "sitekey": sitekey
      }
    }),
    type: 'POST',
    contentType: 'application/json; charset=utf-8',
    success: function (data, textStatus, xhr) {
      var json_data = JSON.parse(JSON.stringify(data));
      console.log(json_data["success"]);
      if (json_data["success"] === "true") {
        document.getElementById(
            "scoreButton").innerText = json_data["data"]["score"];
        // document.getElementById("recaptcha_form").submit();
      }
    },
    error: function (data, textStatus, xhr) {
      var json_data = JSON.parse(JSON.stringify(data));
      addMessage("Got Internal error...");
      console.log(json_data["data"]["error_msg"]);
    }
  })
}

var verifyCallback = function (token) {
  var recaptchaDiv = document.getElementById('recaptcha_render_div');
  httpRequest(token, recaptchaDiv.getAttribute("data-action"),
      recaptchaDiv.getAttribute("data-sitekey"));
};

var onloadCallback = function (event) {
  event.preventDefault();
  document.getElementById('submitBtn').style.display = "none";
  recaptcha_render_div = document.getElementById('recaptcha_render_div');
  recaptcha_render_div.style.display = "block";
  grecaptcha.enterprise.render(recaptcha_render_div, {
    'sitekey': recaptcha_render_div.getAttribute("data-sitekey"),
    'callback': verifyCallback,
    'theme': 'light'
  });
};

function addMessage(message) {
  var myDiv = document.createElement("div");
  myDiv.id = 'div_id';
  myDiv.innerHTML = message + "<p>";
  document.body.appendChild(myDiv);
}