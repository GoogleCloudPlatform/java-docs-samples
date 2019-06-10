<!--
Copyright 2019 Google LLC
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at
      http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
-->


<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%@ page import="com.example.guestbook.Greeting" %>
<%@ page import="com.example.guestbook.Guestbook" %>
<%@ page import="java.util.List" %>

<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
  <link href='//fonts.googleapis.com/css?family=Roboto' rel='stylesheet' type='text/css'>
  <title>Guestbook - Google App Engine Standard</title>
</head>
<body>
  <%
    // Get the guestbook name.
    String guestbookName = request.getParameter("guestbookName");
    if (guestbookName == null || guestbookName.equals(null) || guestbookName.equals("")) {
        guestbookName = "default";
    }
    pageContext.setAttribute("guestbookName", guestbookName);

    // Create a Guestbook object.
    Guestbook theBook = new Guestbook(guestbookName);

    // Run an query to ensure we see the most up-to-date
    // view of the Greetings belonging to the selected Guestbook.
    List<Greeting> greetings = theBook.getGreetings();
  %>

  <h1>Welcome to the '${fn:escapeXml(guestbookName)}' Guestbook</h1>

  <%-- Switch Guestbook --%>
  <form action="/index.jsp" method="get">
    <div><input type="text" name="guestbookName" value="${fn:escapeXml(guestbookName)}"/></div>
    <div><input type="submit" value="Switch Guestbook"/></div>
  </form>

  <%-- Guestbook form --%>
  <h3>Write Your Greeting</h3>
  <%-- [START gae_java11_form] --%>
  <form method="POST" action="/sign">
    <div>
      <label for="name">Name</label>
    </div>
    <div>
      <input type="text" name="name" id="name" size="50" value="${fn:escapeXml(name)}"/>
    </div>

    <div>
      <label for="content">Message</label>
    </div>
    <div>
      <textarea name="content" id="content" rows="10" cols="50">${fn:escapeXml(content)}</textarea>
    </div>

    <div><input type="submit" value="Sign"/></div>
    <input type="hidden" name="guestbookName" value="${fn:escapeXml(guestbookName)}"/>
  </form>
  <%-- [END gae_java11_form] --%>

  <%-- List greetings --%>
  <h3>Greetings:</h3>

  <% if (greetings.isEmpty()) { %>
  <p>The guestbook has no greetings.</p>
  <%
    } else {
      for (Greeting greeting : greetings) {
        pageContext.setAttribute("greeting_content", greeting.content);
        pageContext.setAttribute("greeting_date", greeting.date);

        String author;
        if (greeting.authorName.equals("")) {
          author = "Anonymous";
        } else {
          author = greeting.authorName;
        }
        pageContext.setAttribute("greeting_name", author);
  %>
        <div>
          <b>${fn:escapeXml(greeting_name)}</b>
          <p>${fn:escapeXml(greeting_date)}</p>
          <blockquote>${fn:escapeXml(greeting_content)}</blockquote>
        </div>
  <%
      }
    }
  %>

</body>
</html>
