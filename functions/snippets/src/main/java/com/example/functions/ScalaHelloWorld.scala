package com.example.functions

import com.google.cloud.functions.{HttpFunction, HttpRequest, HttpResponse}

class ScalaHelloWorld extends HttpFunction {
  override def service(httpRequest: HttpRequest, httpResponse: HttpResponse): Unit = {
    httpResponse.getWriter.write("Hello World!")
  }
}
