package functions

// [START functions_helloworld_background]
import java.util.logging.Logger

import com.google.cloud.functions.{BackgroundFunction, Context, HttpRequest}

class ScalaHelloBackground extends BackgroundFunction[HttpRequest] {

  val LOGGER = Logger.getLogger(this.getClass.getName)

  override def accept(t: HttpRequest, context: Context): Unit = {
    // name's default value is "world"
    var name = t.getFirstQueryParameter("name").orElse("world")
    LOGGER.info(String.format("Hello %s!", name))
  }
}
// [END functions_helloworld_background]
