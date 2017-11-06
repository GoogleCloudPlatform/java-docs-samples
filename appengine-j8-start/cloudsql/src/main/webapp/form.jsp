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


<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>
<div>
  <c:choose>
    <c:when test="${id == null}">
      <h2>Create a new blog post</h2>
      <form method="POST" action="/create">
      <div>
        <label for="author">Author</label>
        <select name="blogContent_id">
          <c:forEach items="${users}" var="user">
          <option value="${user.key}">${user.value}</option>
          </c:forEach>
        </select>
      </div>
    </c:when>
    <c:otherwise>
      <h2><c:out value="${pagetitle}" /></h2>
      <form method="POST" action="/update">
      <input type="hidden" name="blogContent_id" value="${id}">
    </c:otherwise>
  </c:choose>

    <div>
      <label for="title">Title</label>
      <input type="text" name="blogContent_title" id="title" size="40" value="${title}" />
    </div>

    <div>
      <label for="description">Post content</label>
      <textarea name="blogContent_description" id="description" rows="10" cols="50">${body}</textarea>
    </div>

    <button type="submit">Save</button>
  </form>
</div>
