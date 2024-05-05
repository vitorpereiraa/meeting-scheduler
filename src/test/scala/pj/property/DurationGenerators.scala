package pj.property

import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}
import pj.domain.SimpleTypes.Duration

object DurationGenerators extends Properties("DurationGenerators"):

  def genDuration: Gen[Duration] = for {
    hour <- Gen.chooseNum(0, 23)
    minute <- Gen.chooseNum(0, 59)
    hourStr = hour.toString.padTo(2, '0')
    minuteStr = minute.toString.padTo(2, '0')
    duration <- Duration.from(s"$hourStr:$minuteStr").fold(_ => Gen.fail, Gen.const)
  } yield duration

  property("All duration must have an hour between 0 and 23 and minutes between 0 and 59") =
    forAll(genDuration) { d =>
      d.getHour >= 0 && d.getHour <= 23 && d.getMinute >= 0 && d.getMinute <= 59
    }

  property("Duration hour and minute must always be two digits") =
    forAll(genDuration) { d =>
      val hour = d.toString.trim.split(":")(0)
      val minute = d.toString.trim.split(":")(1)
      (hour.length == 2 && minute.length == 2)
    }

