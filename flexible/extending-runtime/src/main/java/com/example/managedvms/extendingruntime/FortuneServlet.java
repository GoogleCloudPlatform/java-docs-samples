/**
 * Copyright 2015 Google Inc. All Rights Reserved.
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

package com.example.managedvms.extendingruntime;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// [START example]
@SuppressWarnings("serial")
@WebServlet(name = "fortune", value = "")
public class FortuneServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    PrintWriter out = resp.getWriter();
    String fortune = getFortune();
    out.println(fortune);
  }

  private String getFortune() throws IOException {
    File fort = new File("/usr/games/fortune");
    if (!fort.exists()) {
      return "It seems that the /usr/games/fortune application is not installed on your system.";
    }
    ProcessBuilder pb = new ProcessBuilder(fort.getAbsolutePath());
    File file = File.createTempFile("tmp_fortune", null);
    pb.redirectOutput(file);
    Process process = pb.start();
    try {
      process.waitFor();
    } catch (InterruptedException ex) {
      return ex.getMessage();
    }
    String fortune = "";
    String line;
    BufferedReader br = new BufferedReader(new FileReader(file));
    while ((line = br.readLine()) != null) {
      fortune = fortune + "<br/>" + line;
    }
    br.close();
    return fortune;
  }
}
// [END example]
