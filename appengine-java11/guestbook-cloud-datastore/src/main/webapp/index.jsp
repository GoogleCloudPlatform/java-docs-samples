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

    // Create the correct Ancestor key
    Guestbook theBook = new Guestbook(guestbookName);

    // Run an ancestor query to ensure we see the most up-to-date
    // view of the Greetings belonging to the selected Guestbook.
    List<Greeting> greetings = theBook.getGreetings();
  %>

  <h1>Welcome to the '${fn:escapeXml(guestbookName)}' Guestbook</h1>

  <%-- Switch Guestbook --%>
  <form action="/index.jsp" method="get">
    <div><input type="text" name="guestbookName" value="${fn:escapeXml(guestbookName)}"/></div>
    <div><input type="submit" value="Switch Guestbook"/></div>
  </form>

  <h3>Write Your Greeting</h3>

  <%-- Guestbook form --%>
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
