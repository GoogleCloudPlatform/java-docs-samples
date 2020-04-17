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
<html>
<%@ page import="com.example.flexible.websocket.jettynative.SendServlet" %>
  <head>
    <title>Google App Engine Flexible Environment - WebSocket Echo</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
  </head>
  <body>
    <p>Echo demo</p>
    <form id="echo-form">
      <textarea id="echo-text" placeholder="Enter some text..."></textarea>
      <button type="submit">Send</button>
    </form>

    <div>
      <p>Messages:</p>
      <ul id="echo-response"></ul>
    </div>

    <div>
      <p>Status:</p>
      <ul id="echo-status"></ul>
    </div>
    <script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js"></script>
    <script>
    $(function() {
      var webSocketUri =  "<%=SendServlet.getWebSocketAddress() %>";

      /* Get elements from the page */
      var form = $('#echo-form');
      var textarea = $('#echo-text');
      var output = $('#echo-response');
      var status = $('#echo-status');

      /* Helper to keep an activity log on the page. */
      function log(text){
        status.append($('<li>').text(text))
      }

      /* Establish the WebSocket connection and register event handlers. */
      var websocket = new WebSocket(webSocketUri);

      websocket.onopen = function() {
        log('Connected : ' + webSocketUri);
      };

      websocket.onclose = function() {
        log('Closed');
      };

      websocket.onmessage = function(e) {
        log('Message received');
        output.append($('<li>').text(e.data));
      };

      websocket.onerror = function(e) {
        log('Error (see console)');
        console.log(e);
      };

      /* Handle form submission and send a message to the websocket. */
      form.submit(function(e) {
        e.preventDefault();
        var data = textarea.val();
        websocket.send(data);
      });
    });
    </script>
  </body>
</html>
