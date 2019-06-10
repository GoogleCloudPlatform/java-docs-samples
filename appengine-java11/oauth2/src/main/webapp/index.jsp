<!DOCTYPE html>
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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="com.example.appengine.Utils" %>
<%@ page import="com.google.api.client.auth.oauth2.Credential" %>

<html>
<head>
  <link href='//fonts.googleapis.com/css?family=Roboto' rel='stylesheet' type='text/css'>
  <link type="text/css" rel="stylesheet" href="/stylesheets/main.css"/>
  <title>title</title>
</head>
<body>
<%
  String userId = request.getSession().getId();
  // Use the session id to retrieve access token.
  Credential credential = Utils.newFlow().loadCredential(userId);

  if (credential == null) {
%>
    <a href="/login">Sign In with Google</a>
<%
  } else {
    // Use the credentials to get user info from the OAuth2.0 API.
    String username = Utils.getUserInfo(credential);
%>
  <p> Hello, <%= username %>!</p>

  <form action="/logout" method="post">
    <button>Log Out</button>
  </form>
<%
  }
%>
</body>
</html>
