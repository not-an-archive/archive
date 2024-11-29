package na
package tools

import org.scalacheck.*

object FeeProp extends Properties("na.tools.Fee"):

  import Gen.*
  import Prop.*

  val genMonthlyFeeRate: Gen[Int] =
    choose(Monthly.MinRate, Monthly.MaxRate)

  val genHourlyFeeRate: Gen[Int] =
    choose(Hourly.MinRate, Hourly.MaxRate)

  val genHourlyFee: Gen[Fee] =
    genHourlyFeeRate.map(Hourly.apply)

  val genMonthlyFee: Gen[Fee] =
    genMonthlyFeeRate.map(Monthly.apply)

  val genWeaponizedFee: Gen[Fee] =
    const(Weaponized)

  val genFee: Gen[Fee] =
    oneOf(genHourlyFee, genMonthlyFee, genWeaponizedFee)

  property("Monthly.toString") =
    forAll(genMonthlyFee) { (fee: Fee) =>
      fee.toString.endsWith("/month")
    }

  property("Hourly.toString") =
    forAll(genHourlyFee) { (fee: Fee) =>
      fee.toString.endsWith("/hour")
    }

  property("Weaponized.toString") =
    forAll(genWeaponizedFee) { (fee: Fee) =>
      fee.toString == """weaponized"""
    }

  import io.circe.syntax.*

  property("codec round-trip") =
    forAll(genFee) { (actual: Fee) =>
      actual.asJson.as[Fee].contains(actual)
    }
