/*
 * Copyright 2015 Google Inc.
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

package com.example.appengine.appidentity;

import com.google.apphosting.api.ApiProxy;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
// With @WebServlet annotation the webapp/WEB-INF/web.xml is no longer required.
@WebServlet(
    name = "appidentity",
    description = "AppIdentity: Get the Host Name",
    urlPatterns = "/appidentity/identity"
)
public class IdentityServlet extends HttpServlet {

  // [START gae_java8_app_identity_versioned_hostnames]
  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    resp.setContentType("text/plain");
    ApiProxy.Environment env = ApiProxy.getCurrentEnvironment();
    resp.getWriter().print("default_version_hostname: ");
    resp.getWriter()
        .println(env.getAttributes().get("com.google.appengine.runtime.default_version_hostname"));
  }
  // [END gae_java8_app_identity_versioned_hostnames]
}
