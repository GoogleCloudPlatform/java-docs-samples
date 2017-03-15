package com.example.flexible;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServletRequest;

public class SamplesIndex {

  private static Set<String> ignorePatterns = new HashSet<>();

  static {
    ignorePatterns.addAll(Arrays.asList("jsp", "default", "cron"));
  }

  public static List<String> getSamples(HttpServletRequest request) {
    List<String> samplesList = new ArrayList<>();
    StringBuilder sb = new StringBuilder();
    Map<String, ? extends ServletRegistration> servletRegistrations =
        request.getServletContext().getServletRegistrations();
    for (Map.Entry<String, ? extends ServletRegistration> registrationEntry :
        servletRegistrations.entrySet()) {
      if (!ignorePatterns.contains(registrationEntry.getKey())) {
        for (String mapping : registrationEntry.getValue().getMappings()) {
          sb.append("<a href=");
          sb.append(mapping);
          sb.append(".html");
          sb.append(">");
          sb.append(registrationEntry.getKey());
          sb.append("</a>");
          samplesList.add(sb.toString());
          sb.setLength(0);
        }
      }
    }
    return samplesList;
  }
}