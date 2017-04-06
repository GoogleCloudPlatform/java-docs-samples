<%@ page import="com.example.flexible.pubsub.Message" %>
<%@ page import="com.example.flexible.pubsub.MessageRepository" %>
<%@ page import="java.util.List" %>

<html>
<title>An example of using PubSub on App Engine Flex</title>
<body>
<h3> Publish a message </h3>
<form action="pubsub/publish" method="POST">
          <label for="payload">Message:</label>
          <input id="payload" type="input" name="payload" />
          <input id="submit"  type="submit" value="Send" />
</form>
<h3> Last received messages </h3>
<%! List<Message> messages = MessageRepository.getInstance().retrieve(10); %>
<table>
  <tr>
  <td>Id</td>
  <td>Message</td>
  <td>Publish time</td>
  </tr>
  <%
  for (Message message : messages) {%>
    <tr>
    <td><input value=<%=message.getId()%>></td>
    <td><input value=<%=message.getData()%>></td>
    <td><input value=<%=message.getPublishTime()%>></td>
    </tr>
  <%}%>
</table>
</body>
</html>