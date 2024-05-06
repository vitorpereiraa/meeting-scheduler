package pj.property

import org.scalacheck.Prop.forAll
import org.scalacheck.*
import pj.domain.SimpleTypes.*
import pj.domain.*

object Generators extends Properties("Generators"):

  val MIN_NUMBER = 1
  val MAX_NUMBER = 999
  val MIN_NAME_LENGTH = 1
  val MAX_NAME_LENGTH = 100
  val MIN_PREF_LIMIT = 1
  val MAX_PREF_LIMIT = 5
  val MAX_SUM_PREF_LIMIT = 10000
  val MIN_HOUR = 0
  val MAX_HOUR = 23
  val MIN_MINUTE = 0
  val MAX_MINUTE = 59
  val HOUR_MINUTE_LENGTH = 2

  // Generators

  private def preferenceGen: Gen[Preference] =
    for
      num <- Gen.chooseNum(MIN_PREF_LIMIT, MAX_PREF_LIMIT)
      pref <- Preference.from(num).fold(_ => Gen.fail, x => Gen.const(x))
    yield pref

  private def SummedPreferenceGen: Gen[SummedPreference] =
    for
      num <- Gen.chooseNum(MIN_PREF_LIMIT, MAX_SUM_PREF_LIMIT)
      sum_pref <- SummedPreference.from(num).fold(_ => Gen.fail, x => Gen.const(x))
    yield sum_pref

  private def genDuration: Gen[Duration] = for {
      hour <- Gen.chooseNum(MIN_HOUR, MAX_HOUR)
      minute <- Gen.chooseNum(MIN_MINUTE, MAX_MINUTE)
      hourStr = hour.toString.padTo(2, '0')
      minuteStr = minute.toString.padTo(2, '0')
      duration <- Duration.from(s"$hourStr:$minuteStr").fold(_ => Gen.fail, Gen.const)
    } yield duration

  private def nameGenerator[A](getName: String => Result[A]): Gen[A] =
    for
      nameSize <- Gen.chooseNum(MIN_NAME_LENGTH, MAX_NAME_LENGTH)
      nameChars <- Gen.listOfN(nameSize, Gen.alphaChar)
      name <- getName(nameChars.mkString).fold(_ => Gen.fail, hn => Gen.const(hn))
    yield name

  private def titleGen: Gen[Title] =
    for
      num <- Gen.chooseNum(MIN_NUMBER, MAX_NUMBER)
      t <- nameGenerator(x => Title.from(x + num))
    yield t

  private def studentGen: Gen[Student] =
    for
      num <- Gen.chooseNum(MIN_NUMBER, MAX_NUMBER)
      student <- Student.from("Student" + s"$num").fold(_ => Gen.fail, Gen.const)
    yield student

  private def nameGen: Gen[Name] =
    for
      name <- nameGenerator(x => Name.from(x))
    yield name

  // TODO: Add more generators


  // Properties

  property("Preference must be between 1 and 5") =
    forAll(preferenceGen)(pref => {
      pref.to >= MIN_PREF_LIMIT && pref.to <= MAX_PREF_LIMIT
    })

  property("SummedPreference must be greater or equal than 1") =
    forAll(SummedPreferenceGen)(sum_pref => {
      sum_pref.to >= MIN_PREF_LIMIT
    })

  property("All duration must have an hour between 0 and 23 and minutes between 0 and 59") =
    forAll(genDuration) { d =>
      d.getHour >= MIN_HOUR && d.getHour <= MAX_HOUR && d.getMinute >= MIN_MINUTE && d.getMinute <= MAX_MINUTE
    }

  property("Duration hour and minute must always be two digits") =
    forAll(genDuration) { d =>
      val hour = d.toString.trim.split(":")(0)
      val minute = d.toString.trim.split(":")(1)
      hour.length == HOUR_MINUTE_LENGTH && minute.length == HOUR_MINUTE_LENGTH
    }

  property("Title must be characters + numbers") =
    forAll(titleGen)({ title =>
      title.to.matches("^[a-zA-Z0-9]*$")
    })

  property("Student must be Student + numbers") =
    forAll(studentGen)({
      student => student.to.matches("^[a-zA-Z0-9]*$")
    })

  property("Name must be characters non empty") =
    forAll(nameGen)({
      name => name.to.nonEmpty
    })

  // TODO: Add more properties