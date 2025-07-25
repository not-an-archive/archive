package naa
package tools

import cats.effect.testing.scalatest.*

import org.scalatest.*
import flatspec.*
import matchers.*

class SmokeTest extends AsyncFlatSpec with should.Matchers with AsyncIOSpec:

  "naa.tools.SmokeTest" should "decode the project's local authors.json file correctly" in {
    Authors
      .localAuthors
      .assertNoException
  }