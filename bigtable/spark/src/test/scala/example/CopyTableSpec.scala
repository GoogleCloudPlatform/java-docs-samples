package example

import org.scalatest._
import flatspec._
import matchers._

class CopyTableSpec extends AnyFlatSpec with should.Matchers {
  "CopyTable" should "display help when the required command-line arguments are missing" in {
    CopyTable.main(Array.empty)
  }
}
