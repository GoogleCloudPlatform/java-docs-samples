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

package com.example.disk;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@SuppressWarnings("serial")
@WebServlet(name = "disk", value = "")
public class DiskServlet extends HttpServlet {

  @Override
  public void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
    // store only the first two octets of a users ip address
    String userIp = req.getRemoteAddr();
    InetAddress address = InetAddress.getByName(userIp);
    if (address instanceof Inet6Address) {
      // nest indexOf calls to find the second occurrence of a character in a string
      // an alternative is to use Apache Commons Lang: StringUtils.ordinalIndexOf()
      userIp = userIp.substring(0, userIp.indexOf(":", userIp.indexOf(":") + 1)) + ":*:*:*:*:*:*";
    } else if (address instanceof Inet4Address) {
      userIp = userIp.substring(0, userIp.indexOf(".", userIp.indexOf(".") + 1)) + ".*.*";
    }
    Path tmpFile = Paths.get("/tmp/seen.txt");
    Files.write(
        tmpFile,
        (userIp + "\n").getBytes(),
        StandardOpenOption.CREATE,
        StandardOpenOption.APPEND);
    StringBuffer sb = new StringBuffer();
    List<String> strings = Files.readAllLines(tmpFile, StandardCharsets.US_ASCII);
    for (String s : strings) {
      sb.append(s + "\n");
    }

    String instanceId =
        System.getenv().containsKey("GAE_INSTANCE") ? System.getenv("GAE_INSTANCE") : "1";
    PrintWriter out = resp.getWriter();
    resp.setContentType("text/plain");
    out.print("Instance: " + instanceId + "\nSeen:\n" + sb.toString());
  }
}
