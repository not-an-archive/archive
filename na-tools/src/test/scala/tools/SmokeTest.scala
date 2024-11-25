package na
package tools

import cats.effect.testing.scalatest.*

import org.scalatest.*
import flatspec.*
import matchers.*

class SmokeTest extends AsyncFlatSpec with should.Matchers with AsyncIOSpec:

  "SmokeTest" should "decode the project's authors.json input correctly" in {
    Authors
      .localAuthors
      .assertNoException
  }