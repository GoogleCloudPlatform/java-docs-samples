<%@ page import="java.util.List" %>
<%@ page import="com.example.cloudsql.Vote" %>
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

<%
    Integer tabVoteCt = (Integer) request.getAttribute("tabVoteCt");
    Integer spaceVoteCt = (Integer) request.getAttribute("spaceVoteCt");
    List<Vote> recentVotes = (List<Vote>) request.getAttribute("recentVotes");
    int voteDiff = 0;
    String leadTeam = "";
    if (!tabVoteCt.equals(spaceVoteCt)) {
      if (tabVoteCt > spaceVoteCt) {
        leadTeam = "TABS";
        voteDiff = tabVoteCt - spaceVoteCt;
      } else {
        leadTeam = "SPACES";
        voteDiff = spaceVoteCt - tabVoteCt;
      }
    }
%>
<div class="section">
    <div class="center">
        <h4>
            <% if(voteDiff != 0) { %>
            <%= leadTeam %> are winning by <%= voteDiff %> <%= voteDiff > 1 ? "votes" : "vote" %>.
            <% } else { %>
            TABS and SPACES are tied!
            <% } %>
        </h4>
    </div>
    <div class="row center">
        <div class="col s6 m5 offset-m1">
            <div class="card-panel <%= leadTeam.equals("TABS") ? "green lighten-3" : "" %>">
                <i class="material-icons large">keyboard_tab</i>
                <h3><%= tabVoteCt %> votes</h3>
                <button id="voteTabs" class="btn green">Vote for TABS</button>
            </div>
        </div>
        <div class="col s6 m5">
            <div  class="card-panel <%= leadTeam.equals("SPACES") ? "blue lighten-3" : ""  %>">
                <i class="material-icons large">space_bar</i>
                <h3><%= spaceVoteCt %> votes</h3>
                <button id="voteSpaces" class="btn blue">Vote for SPACES</button>
            </div>
        </div>
    </div>
    <h4 class="header center">Recent Votes</h4>
    <ul class="container collection center">
        <% for(Vote v : recentVotes) { %>
            <li class="collection-item avatar">
                <% if(v.getCandiate().equals("tabs")) { %>
                <i class="material-icons circle green">keyboard_tab</i>
                <% } else { %>
                <i class="material-icons circle blue">space_bar</i>
                <% } %>
                <span class="title">A vote for <b><%= v.getCandiate().toUpperCase() %></b></span>
                <p>was cast at <%= v.getTimeCast() %>.</p>
            </li>
        <% } %>
    </ul>
</div>
</body>
<footer>
    <script>
      function vote(team) {
        var xhr = new XMLHttpRequest();
        xhr.onreadystatechange = function () {
          var msg = "";
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
        vote("tabs");
      });
      document.getElementById("voteSpaces").addEventListener("click", function () {
        vote("spaces");
      });
    </script>
</footer>
</html>
