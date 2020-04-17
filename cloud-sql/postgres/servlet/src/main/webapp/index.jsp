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
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<html lang="en">
<head>
    <title>Tabs VS Spaces</title>
    <link rel="stylesheet"
          href="https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/css/materialize.min.css">
    <link href="https://fonts.googleapis.com/icon?family=Material+Icons" rel="stylesheet">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/materialize/1.0.0/js/materialize.min.js"></script>
</head>
<body>
<nav class="red lighten-1">
    <div class="nav-wrapper">
        <a href="#" class="brand-logo center">Tabs VS Spaces</a>
    </div>
</nav>
<div class="section">
    <div class="center">
        <h4>
            <c:choose>
                <c:when test="${tabCount == spaceCount}">
                    TABS and SPACES are evenly matched!
                </c:when>
                <c:when test="${tabCount > spaceCount}">
                    TABS are winning by <c:out value="${tabCount - spaceCount}"/>
                    <c:out value='${tabCount - spaceCount > 1 ? "votes" : "vote"}'/>!
                </c:when>
                <c:when test="${tabCount < spaceCount}">
                    SPACES are winning by <c:out value="${spaceCount - tabCount}"/>
                    <c:out value='${spaceCount - tabCount > 1 ? "votes" : "vote"}'/>!!
                </c:when>
            </c:choose>
        </h4>
    </div>
    <div class="row center">
        <div class="col s6 m5 offset-m1">
            <c:if test="${tabCount > spaceCount}">
                <c:set value="green lighten-3" var="tabWinClass"></c:set>
            </c:if>
            <div class="card-panel ${tabWinClass}">
                <i class="material-icons large">keyboard_tab</i>
                <h3><c:out value="${tabCount}"/> votes</h3>
                <button id="voteTabs" class="btn green">Vote for TABS</button>
            </div>
        </div>
        <div class="col s6 m5">
            <c:if test="${tabCount < spaceCount}">
                <c:set value="blue lighten-3" var="spaceWinClass"></c:set>
            </c:if>
            <div class="card-panel ${spaceWinClass}">
                <i class="material-icons large">space_bar</i>
                <h3><c:out value="${spaceCount}"/> votes</h3>
                <button id="voteSpaces" class="btn blue">Vote for SPACES</button>
            </div>
        </div>
    </div>
    <h4 class="header center">Recent Votes</h4>
    <ul class="container collection center">
        <c:forEach items="${recentVotes}" var="vote">
            <li class="collection-item avatar">
                <c:choose>
                    <c:when test='${vote.getCandidate().equals("TABS")}'>
                        <i class="material-icons circle green">keyboard_tab</i>
                    </c:when>
                    <c:when test='${vote.getCandidate().equals("SPACES")}'>
                        <i class="material-icons circle blue">space_bar</i>
                    </c:when>
                </c:choose>
                <span class="title">
                    A vote for <b><c:out value="${vote.getCandidate()}"/></b>
                </span>
                <p>was cast at <c:out value="${vote.getTimeCast()}"/>.</p>
            </li>
        </c:forEach>
    </ul>
</div>
<script>
  function vote(team) {
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function () {
      if (this.readyState == 4) {
        if (!window.alert(this.responseText)) {
          window.location.reload();
        }
      }
    };
    xhr.open("POST", "/", true);
    xhr.setRequestHeader("Content-Type", "application/x-www-form-urlencoded");
    xhr.send("team=" + team);
  }

  document.getElementById("voteTabs").addEventListener("click", function () {
    vote("TABS");
  });
  document.getElementById("voteSpaces").addEventListener("click", function () {
    vote("SPACES");
  });
</script>
</body>
</html>
