<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="com.google.api.client.googleapis.auth.oauth2.GoogleIdToken" %>
<%@ page import="io.jsonwebtoken.Claims" %>
<!-- [START_EXCLUDE] -->
<%--
  ~ Copyright (c) 2019 Google Inc.
  ~
  ~ Licensed under the Apache License, Version 2.0 (the "License"); you
  ~ may not use this file except in compliance with the License. You may
  ~ obtain a copy of the License at
  ~
  ~     http://www.apache.org/licenses/LICENSE-2.0
  ~
  ~ Unless required by applicable law or agreed to in writing, software
  ~ distributed under the License is distributed on an "AS IS" BASIS,
  ~ WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
  ~ implied. See the License for the specific language governing
  ~ permissions and limitations under the License.
  --%>
<!-- [END_EXCLUDE] -->
<!doctype html>
<html>
<head>
  <title>Pub/Sub Java on Google App Engine Standard Environment</title>
</head>
<body>
<%List<GoogleIdToken.Payload> messages = (List)request.getAttribute("messages");%>
<%List<String> tokens = (List)request.getAttribute("tokens");%>
<%List<Claims> claims = (List)request.getAttribute("claims");%>
<div>
  <p>
  <%if(null != messages && messages.size()>0) { %>
  <li>messages:</li>
  <%for (GoogleIdToken.Payload msg:messages) { %>
    <li><%=msg.toString()%></li>
  <%}%>
  <%}%>
  </p>
  <p>
    <%if(null != tokens && tokens.size()>0) { %>
    <li>tokens:</li>
    <%for (String token:tokens) { %>
      <li><%=token%></li>
    <%}%>
    <%}%>
    </p>
  <p>
      <%if(null != claims && claims.size()>0) { %>
      <li>Claims:</li>
      <%for (Claims claim:claims) { %>
        <li><%=claim.toString()%></li>
      <%}%>
      <%}%>
      </p>
  <ul>
  </ul>
  <p><small></small></p>
</div>
<!-- [START form] -->
<form method="post">
  <textarea name="payload" placeholder=""></textarea>
  <input type="submit">
</form>
<!-- [END form] -->
</body>
</html>