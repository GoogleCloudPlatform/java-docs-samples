<!--
 Copyright 2023 Google LLC

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

<%@ page import="com.example.flexible.pubsub.PubSubHome" %>
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
