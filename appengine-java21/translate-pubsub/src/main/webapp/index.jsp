<%@ page import="com.example.appengine.translatepubsub.PubSubHome" %>

<html>
  <head>
    <meta http-equiv="refresh" content="10">
  </head>
  <title>An example of using PubSub on App Engine Standard</title>
  <body>
    <h3>Publish a message</h3>
    <h4><a href="https://cloud.google.com/translate/docs/languages">See more language codes</a></h4>
    <form action="pubsub/publish" method="POST">
      <label for="payload">Message:</label>
      <input id="payload" type="input" name="payload" />
      <br/>
      <label for="sourceLang">Source Language Code:</label>
      <input id="sourceLang" type="text" name="sourceLang" value="en"/>
      <br/>
      <label for="targetLang">Target Language Code:</label>
      <input id="targetLang" type="text" name="targetLang" value="en"/>
      <br/>
      <input id="submit"  type="submit" value="Send" />
    </form>
    <h3> Last received messages </h3>
    <table border="1" cellpadding="10">
      <tr>
      <th>Id</th>
      <th>Data</th>
      <th>PublishTime</th>
      <th>SourceLang</th>
      <th>TargetLang</th>
      </tr>
      <%= PubSubHome.getReceivedMessages() %>
    </table>
  </body>
</html>
