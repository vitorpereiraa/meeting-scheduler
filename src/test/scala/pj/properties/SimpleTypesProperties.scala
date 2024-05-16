package pj.properties

import org.scalacheck.*
import org.scalacheck.Prop.forAll
import pj.domain.*
import pj.domain.SimpleTypes.*

object SimpleTypesProperties extends Properties("SimpleTypesProperties"):

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
  val MIN_SECOND = 0
  val MAX_SECOND = 59
  val HOUR_MINUTE_LENGTH = 2
  val MIN_YEAR = 1000
  val MAX_YEAR = 9999
  val MIN_MONTH = 1
  val MAX_MONTH = 12
  val MIN_DAY = 1

  // Generators
  def preferenceGen: Gen[Preference] =
    for
      num <- Gen.chooseNum(MIN_PREF_LIMIT, MAX_PREF_LIMIT)
      pref <- Preference.from(num).fold(_ => Gen.fail, x => Gen.const(x))
    yield pref

  def summedPreferenceGen: Gen[SummedPreference] =
    for
      num <- Gen.chooseNum(MIN_PREF_LIMIT, MAX_SUM_PREF_LIMIT)
      sum_pref <- SummedPreference.from(num).fold(_ => Gen.fail, x => Gen.const(x))
    yield sum_pref

  def durationGen: Gen[Duration] = for {
      hour <- Gen.chooseNum(MIN_HOUR, MAX_HOUR)
      minute <- Gen.chooseNum(MIN_MINUTE, MAX_MINUTE)
      hourStr = hour.toString.reverse.padTo(2, '0').reverse
      minuteStr = minute.toString.reverse.padTo(2, '0').reverse
      duration <- Duration.from(s"$hourStr:$minuteStr").fold(_ => Gen.fail, Gen.const)
    } yield duration

  def nameGenerator[A](getName: String => Result[A]): Gen[A] =
    for
      nameSize <- Gen.chooseNum(MIN_NAME_LENGTH, MAX_NAME_LENGTH)
      nameChars <- Gen.listOfN(nameSize, Gen.alphaChar)
      name <- getName(nameChars.mkString).fold(_ => Gen.fail, hn => Gen.const(hn))
    yield name

  def titleGen: Gen[Title] =
    for
      num <- Gen.chooseNum(MIN_NUMBER, MAX_NUMBER)
      t <- nameGenerator(x => Title.from(x + num))
    yield t

  def studentGen: Gen[Student] =
    for
      num <- Gen.chooseNum(MIN_NUMBER, MAX_NUMBER)
      student <- Student.from("Student" + s"$num").fold(_ => Gen.fail, Gen.const)
    yield student

  def studentGen(num: Int): Gen[Student] =
    for
      student <- Student.from("Student" + s"$num").fold(_ => Gen.fail, Gen.const)
    yield student

  def nameGen: Gen[Name] =
    for
      name <- nameGenerator(x => Name.from(x))
    yield name

  def dateTimeGen: Gen[DateTime] =
    for
      year         <- Gen.chooseNum(MIN_YEAR, MAX_YEAR)
      month        <- Gen.chooseNum(MIN_MONTH, MAX_MONTH)
      day          <- Gen.chooseNum(MIN_DAY, daysInMonth(year, month))
      hour         <- Gen.chooseNum(MIN_HOUR, MAX_HOUR)
      minute       <- Gen.chooseNum(MIN_MINUTE, MAX_MINUTE)
      seconds      <- Gen.chooseNum(MIN_SECOND, MAX_SECOND)
      monthStr     = month.toString.reverse.padTo(2, '0').reverse
      dayStr       = day.toString.reverse.padTo(2, '0').reverse
      hourStr      = hour.toString.reverse.padTo(2, '0').reverse
      minuteStr    = minute.toString.reverse.padTo(2, '0').reverse
      secondsStr   = seconds.toString.reverse.padTo(2, '0').reverse
      dateTime     <- DateTime.from(s"$year-$monthStr-${dayStr}T$hourStr:$minuteStr:$secondsStr").fold(_ => Gen.fail, Gen.const)
    yield dateTime

  def daysInMonth(year: Int, month: Int): Int =
    val febDays = if (year % 4 == 0 && (year % 100 != 0 || year % 400 == 0)) 29 else 28 // feb days depend if it is a leap year
    val days = Array(31, febDays , 31, 30, 31, 30, 31, 31, 30, 31, 30, 31)
    days(month - 1)

  // Properties
  property("Preference must be between 1 and 5") =
    forAll(preferenceGen)(pref => {
      pref.to >= MIN_PREF_LIMIT && pref.to <= MAX_PREF_LIMIT
    })

  property("SummedPreference must be greater or equal than 1") =
    forAll(summedPreferenceGen)(sum_pref => {
      sum_pref.to >= MIN_PREF_LIMIT
    })

  property("All duration must have an hour between 0 and 23 and minutes between 0 and 59") =
    forAll(durationGen) { d =>
      d.getHour >= MIN_HOUR && d.getHour <= MAX_HOUR && d.getMinute >= MIN_MINUTE && d.getMinute <= MAX_MINUTE
    }

  property("Duration hour and minute must always be two digits") =
    forAll(durationGen) { d =>
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

  property("All date times must have valid dates and times") =
    forAll(dateTimeGen) { d =>
      val year = d.toLocalDateTime.getYear
      val month = d.toLocalDateTime.getMonth.getValue
      val day = d.toLocalDateTime.getDayOfMonth
      val hour = d.toLocalDateTime.getHour
      val minute = d.toLocalDateTime.getMinute
      val second = d.toLocalDateTime.getSecond

      (year >= MIN_YEAR && year <= MAX_YEAR) &&
      (month >= MIN_MONTH && month <= MAX_MONTH) &&
      (day >= MIN_DAY && day <= daysInMonth(year, month)) &&
      (hour >= MIN_HOUR && hour <= MAX_HOUR) &&
      (minute >= MIN_MINUTE && minute <= MAX_MINUTE) &&
      (second >= MIN_SECOND && second <= MAX_SECOND)
    }