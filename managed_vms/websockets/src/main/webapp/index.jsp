<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!doctype html>
<html>
<head>
  <title>Google Managed VMs - Java Websockets Echo</title>
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
</head>
<body>
  <!-- [START form] -->
  <p>Echo demo</p>
  <form id="echo-form">
    <textarea id="echo-text" placeholder="Enter some text..."></textarea>
    <button type="submit">Send</button>
  </form>
  <div>
    <p>Response:</p>
    <pre id="echo-response"></pre>
  </div>
  <div>
    <p>Status:</p>
    <pre id="echo-status"></pre>
  </div>
  <!-- [END form] -->
  <script src="https://ajax.googleapis.com/ajax/libs/jquery/2.1.4/jquery.min.js"></script>
  <script>
  $(function() {
    /* The external ip is determined by main.py and passed into the template. */
    var externalIp = '${applicationScope.external_ip}';
    var webSocketUri =  "ws://" + externalIp + ':65080/echo';
    /* Get elements from the page */
    var form = $('#echo-form');
    var textarea = $('#echo-text');
    var output = $('#echo-response');
    var status = $('#echo-status');
    /* Helper to keep an activity log on the page. */
    function log(text){
      status.text(status.text() + text + '\n');
    }
    /* Establish the WebSocket connection and register event handlers. */
    var websocket = new WebSocket(webSocketUri);
    websocket.onopen = function() {
      log('Connected');
    };
    websocket.onclose = function(e) {
      log('Closed');
      output.text(e.code + e.reason);
    };
    websocket.onmessage = function(e) {
      log('Message received');
      output.text(e.data);
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
