<!DOCTYPE html>
<!--
  Copyright 2016 Google Inc.

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
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>Guestbook</title>
  </head>
  <body>
    <h2>Latest Greetings</h2>
    <c:forEach items="${greetings}" var="greeting">
      <p>
        ${greeting.content}<br>
        Posted: ${greeting.date}
      </p>
    </c:forEach>

    <h2>Add Greeting</h2>
    <form method="post">
      <p>
        <label for="greeting-content">Greeting</label>
        <input type="text" name="content" id="greeting-content">
      </p>
      <p>
        <input type="submit" value="Sign Guestbook">
      </p>
    </form>
  </body>
</html>

