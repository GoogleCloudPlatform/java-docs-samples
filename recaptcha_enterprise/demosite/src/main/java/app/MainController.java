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

package app;

import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;
import recaptcha.Recaptcha;

@Controller
@RequestMapping
public class MainController {

  @GetMapping(value = "/")
  public static ModelAndView login() {
    ModelMap map = new ModelAndView().getModelMap();
    map.put("site_key", System.getenv("SITE_KEY"));
    return new ModelAndView("login", map);
  }

  @GetMapping(value = "/signup")
  public static ModelAndView signup() {
    ModelMap context = new ModelAndView().getModelMap();
    context.put("checkbox_site_key", System.getenv("CHECKBOX_SITE_KEY"));
    return new ModelAndView("signup", context);
  }

  @PostMapping(value = "/create_assessment", produces = "application/json")
  public static @ResponseBody
  ResponseEntity<String> createAssessment(@RequestBody String json) {
    JSONObject jsonObject = new JSONObject(new JSONTokener(json));
    String projectId = System.getenv("GOOGLE_CLOUD_PROJECT");
    JSONObject result = Recaptcha.execute(projectId, jsonObject);
    final HttpHeaders httpHeaders = new HttpHeaders();
    httpHeaders.setContentType(MediaType.APPLICATION_JSON);
    return new ResponseEntity<>(result.toString(), httpHeaders, HttpStatus.OK);
  }

}
