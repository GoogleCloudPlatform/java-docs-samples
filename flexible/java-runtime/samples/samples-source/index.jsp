<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="java.util.HashSet" %>
<%@ page import="java.util.Set" %>
<%@ page import="java.lang.StringBuilder" %>
<html>
<head>
    <title>GAE Flex Samples</title>
</head>
<body>
<ul>
 <%! static final Set<String> ignorePatterns = new HashSet<>();
    static {
      ignorePatterns.addAll(Arrays.asList("jsp", "default", "cron"));
    }
 %>

     <%! List<String> getSamples(HttpServletRequest request) {
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
              if (!mapping.endsWith(".html")) {
                sb.append(".html");
              }
              sb.append(">");
              sb.append(registrationEntry.getKey());
              sb.append("</a>");
              samplesList.add(sb.toString());
              sb.setLength(0);
            }
          }
        }
        return samplesList;
      } %>
    <%
        for (String sample : getSamples(request)) {
    %>
    <li><%= sample %>
    </li>
    <%
    }
    %>
</ul>
</body>
</html>
