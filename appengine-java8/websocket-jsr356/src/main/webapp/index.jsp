<!DOCTYPE html>
<!--
  Copyright 2018 Google LLC

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
<%@ page import="com.example.flexible.websocket.jsr356.ClientSocket" %>
<html>
  <head>
    <meta http-equiv="refresh" content="10">
  </head>
  <title>Send a message </title>
  <body>
    <h3> Publish a message </h3>
    <form action="send" method="POST">
      <label for="message">Message:</label>
      <input id="message" type="input" name="message" />
      <input id="send"  type="submit" value="Send" />
    </form>
    <h3> Last received messages </h3>
    <%= ClientSocket.getReceivedMessages() %>
  </body>
</html>
