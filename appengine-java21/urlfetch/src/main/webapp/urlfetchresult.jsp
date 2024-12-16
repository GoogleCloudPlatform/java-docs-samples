<%--
Copyright 2016 Google Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<!-- [START base] -->
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html lang="en">
<head>
    <title>URL Fetch sample</title>
    <meta charset="utf-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
</head>
<body>
    <h1 align="center">URL Fetch Sample</h1>
    <c:if test="${not empty joke}">
        <h2>Joke: ${joke}</h2>
    </c:if>

    <c:if test="${not empty error}">
        <h2 style="color:red">${error}</h2>
    </c:if>

    <c:if test="${not empty response}">
      <p>${response}</p>
    </c:if>

    <form method="post">
        <label for="id">&nbsp;&nbsp;ID:</label><input type="text" name="id" value="1"/><br />
        <label for="text">Text:</label><input type="text" name="text" value="Lorem ipsum dolor sit amet, consectetur adipiscing elit."/><br />
        <input type="submit" value="Send"/>
    </form>
</body>
</html>
<!-- [END base]-->
