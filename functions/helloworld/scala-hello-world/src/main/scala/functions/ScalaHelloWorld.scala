package functions

import com.google.cloud.functions.{HttpFunction, HttpRequest, HttpResponse}

// [START functions_helloworld_get]
class ScalaHelloWorld extends HttpFunction {
  override def service(httpRequest: HttpRequest, httpResponse: HttpResponse): Unit = {
    httpResponse.getWriter.write("Hello World!")
  }
}
// [END functions_helloworld_get]