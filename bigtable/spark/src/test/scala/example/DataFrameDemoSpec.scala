package example

import org.scalatest._
import flatspec._
import matchers._

class DataFrameDemoSpec extends AnyFlatSpec with should.Matchers {
  "DataFrameDemo" should "display help when the required command-line arguments are missing" in {
    DataFrameDemo.main(Array.empty)
  }
}
