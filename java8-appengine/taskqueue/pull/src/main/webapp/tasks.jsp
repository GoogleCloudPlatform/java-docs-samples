<!DOCTYPE html>
<!--
  Copyright 2016 Google Inc. All Rights Reserved.
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
<%@ page import="java.util.List" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>

<html>
<head>
    <style type="text/css">
    body {
        background-color: silver;
    }
    </style>
</head>

<body>

<form action="/taskqueue" method="post">
    <div>
        <textarea name="content" placeholder="Enter a task payload" rows="3" cols="60"></textarea></div>
    <div><input type="submit" name="addTask" value="Add Tasks to the Task Queue"/></div>
</form>
<br>
<form action="/taskqueue" method="post">
    <div><input type="submit" name="leaseTask" value="Lease, Process, and Delete Tasks"/></div>
</form>
<br>
<div>
  ${message}
  <br><br>
  <a href="https://console.cloud.google.com/appengine/taskqueues" target="_blank">
    View task queues in the Developers Console</a>
  <br><br>
  <a href="https://console.cloud.google.com/logs" target="_blank">
    View task activity logged in the Developers Console</a>
</div>

</body>
</html>
