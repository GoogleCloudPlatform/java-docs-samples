package com.example.functions

// [START functions_helloworld_background]
import com.google.cloud.functions.{BackgroundFunction, Context, HttpRequest}
import java.util.logging.Logger

class ScalaHelloBackground extends BackgroundFunction[HttpRequest] {

  val LOGGER = Logger.getLogger(this.getClass.getName)

  override def accept(t: HttpRequest, context: Context): Unit = {
    val name = t.getFirstQueryParameter("name").orElse("world")
    LOGGER.info(String.format("Hello %s!", name))
  }
}
// [END functions_helloworld_background]
