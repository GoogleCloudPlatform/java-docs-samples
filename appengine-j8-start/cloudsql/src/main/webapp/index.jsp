<%--
Copyright 2017 Google Inc.

<p>Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
except in compliance with the License. You may obtain a copy of the License at

<p>http://www.apache.org/licenses/LICENSE-2.0

<p>Unless required by applicable law or agreed to in writing, software distributed under the
License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
express or implied. See the License for the specific language governing permissions and
limitations under the License.
--%>

<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
  <head>
    <title>The App Engine Blog</title>
  </head>
  <body>
    <h1>Welcome to the App Engine Blog</h1>
    <c:forEach var="content" items="${posts.key}">
      <c:forEach var="allPosts" items="${content.value}">
        <h2><c:out value="${allPosts['title']}"/></h2>
          <h4>Posted at: <c:out value="${post.key}"/> by <c:out value="${post.value}"/></h4>
          <c:out value="${singlePost[body]}" />
          <p>[<a href="/update?postid=">update</a>] | [<a href="/delete?postid=">delete</a>]</p>
    </c:forEach>
    </c:forEach>
  </body>
</html>
