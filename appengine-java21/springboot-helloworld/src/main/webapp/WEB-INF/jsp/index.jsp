<?xml version="1.0" encoding="UTF-8" ?>
<!--

    Copyright © 2017 the original authors (@michaeltecourt)

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
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ page isELIgnored="false"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<!doctype html>
<html>
<head>
<meta http-equiv="content-type" content="text/html; charset=UTF-8">
<meta charset="UTF-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no" />
<title>Sample Spring Boot Application / App Engine</title>
<script src="/webjars/jquery/jquery.js"></script>
<script src="/webjars/bootstrap/js/bootstrap.min.js"></script>
<link href="/webjars/bootstrap/css/bootstrap.min.css" rel="stylesheet">
<script type="text/javascript">
    nope = null;
</script>
</head>
<body>
  <h1>Sample Spring Boot Application running as an App Engine Java21 Web App.!</h1>
  This is the index.jsp. Try also the following urls:
  <ul>
      <li><a href="/aliens">/aliens</a></li>
      <li><a href="/admin">/admin</a></li>
      <li><a href="/actuator/metrics">actuator/metrics</a></li>
      <li><a href="actuator/metrics/jvm.memory.max">actuator/metrics/jvm.memory.max</a></li>
      <li><a href="actuator/health">actuator/health</a></li>
      <li><a href="actuator/env">actuator/env</a></li>
      <li><a href="actuator/threaddump">actuator/threaddump</a></li>
      <li><a href="actuator/loggers">actuator/loggers</a></li>
      <li><a href="actuator/beans">actuator/beans</a></li>
      <li><a href="actuator/health">actuator/health</a></li>

  </ul>
</body>
</html>
