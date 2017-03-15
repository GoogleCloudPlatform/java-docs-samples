<%@ page import="com.example.flexible.SamplesIndex" %>
<html>
<head>
    <title>GAE Flex Samples</title>
</head>
<body>
<ul>
    <%
        for (String sample : SamplesIndex.getSamples(request)) {
    %>
    <li><%= sample %>
    </li>
    <%
        }
    %>
    <li><a href="/static.html">A static HTML page</a></li>
</ul>
</body>
</html>