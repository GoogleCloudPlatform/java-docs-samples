<!DOCTYPE html>
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
  Credential credential = Utils.newFlow().loadCredential(userId);

  if (credential == null) {
%>
    <a href="/login">Sign In with Google</a>
<%
  } else {
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
