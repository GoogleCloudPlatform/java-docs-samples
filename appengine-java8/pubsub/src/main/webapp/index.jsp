<%@ page import="com.example.appengine.pubsub.PubSubHome" %>
<html>
  <head>
    <meta http-equiv="refresh" content="10">
  </head>
  <title>An example of using PubSub on App Engine Flex</title>
  <body>
    <h3> Publish a message </h3>
    <form action="pubsub/publish" method="POST">
      <label for="payload">Message:</label>
      <input id="payload" type="input" name="payload" />
      <input id="submit"  type="submit" value="Send" />
    </form>
    <h3> Last received messages </h3>
    <table border="1" cellpadding="10">
      <tr>
      <th>Id</th>
      <th>Data</th>
      <th>PublishTime</th>
      </tr>
      <%= PubSubHome.getReceivedMessages() %>
    </table>
  </body>
</html>
