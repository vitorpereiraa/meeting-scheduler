package pj.domain

import org.scalatest.funsuite.AnyFunSuite
import pj.domain.SimpleTypes.*

class SimpleTypesTest extends AnyFunSuite:

  test("Duration should be created from valid string"):
    val duration = Duration.from("10:00")
    assert(duration.isRight)

  test("Duration should fail with invalid string"):
    val duration = Duration.from("invalid")
    assert(duration.isLeft)

  test("Duration should be created from valid hour and minute"):
    val duration = Duration.from(10, 30)
    assert(duration.isRight)

  test("Duration should fail with invalid hour"):
    val duration = Duration.from(25, 30)
    assert(duration.isLeft)

  test("Duration should fail with invalid minute"):
    val duration = Duration.from(10, 60)
    assert(duration.isLeft)

  test("Duration should be created from valid start and end DateTime"):
    for
      start <- DateTime.from("2022-01-01T09:00:00")
      end <- DateTime.from("2022-01-01T10:00:00")
      duration <- Duration.fromBetween(start, end)
    yield assert(duration.to == "01:00:00")

  test("Duration should fail with end DateTime before start DateTime"):
    for
      start <- DateTime.from("2022-01-01T10:00:00")
      end <- DateTime.from("2022-01-01T09:00:00")
      duration = Duration.fromBetween(start, end)
    yield assert(duration.isLeft)

  test("Duration should correctly convert to string"):
    for
      duration <- Duration.from("10:00")
    yield assert(duration.to == "10:00:00")

  test("Duration should correctly convert to minutes"):
    for
      duration <- Duration.from("01:30")
    yield assert(duration.toMinutes == 90)

  test("Duration should correctly compare two durations"):
    for
      duration1 <- Duration.from("10:00")
      duration2 <- Duration.from("11:00")
    yield assert(duration1.isBefore(duration2))

  test("Title should be created from valid string"):
    val title = Title.from("Valid Title")
    assert(title.isRight)

  test("Title should fail with blank string"):
    val title = Title.from(" ")
    assert(title.isLeft)

  test("Name should be created from valid string"):
    val name = Name.from("Valid Name")
    assert(name.isRight)

  test("Name should fail with blank string"):
    val name = Name.from(" ")
    assert(name.isLeft)

  test("Student should be created from valid string"):
    val student = Student.from("Valid Student")
    assert(student.isRight)

  test("Student should fail with blank string"):
    val student = Student.from(" ")
    assert(student.isLeft)

  test("DateTime should be created from valid string"):
    val dateTime = DateTime.from("2022-01-01T10:00:00")
    assert(dateTime.isRight)

  test("DateTime should fail with invalid string"):
    val dateTime = DateTime.from("invalid")
    assert(dateTime.isLeft)

  test("DateTime.to should correctly format DateTime to String"):
    for
      dateTime <- DateTime.from("2022-01-01T09:00:00")
    yield assert(dateTime.to == "2022-01-01T09:00:00")

  test("DateTime.isAfter should correctly compare two DateTimes"):
    for
      dateTime1 <- DateTime.from("2022-01-01T09:00:00")
      dateTime2 <- DateTime.from("2022-01-01T10:00:00")
    yield
      assert(!dateTime1.isAfter(dateTime2))
      assert(dateTime2.isAfter(dateTime1))

  test("DateTime.isBefore should correctly compare two DateTimes"):
    for
      dateTime1 <- DateTime.from("2022-01-01T09:00:00")
      dateTime2 <- DateTime.from("2022-01-01T10:00:00")
    yield
      assert(dateTime1.isBefore(dateTime2))
      assert(!dateTime2.isBefore(dateTime1))

  test("DateTime.isEqual should correctly compare two equal DateTimes"):
    for
      dateTime1 <- DateTime.from("2022-01-01T09:00:00")
      dateTime2 <- DateTime.from("2022-01-01T09:00:00")
    yield assert(dateTime1.isEqual(dateTime2))

  test("DateTime.plus should correctly add Duration to DateTime"):
    for
      dateTime <- DateTime.from("2022-01-01T09:00:00")
      duration <- Duration.from("01:00")
      result = dateTime.plus(duration)
    yield assert(result.to == "2022-01-01T10:00:00")

  test("Preference should be created from valid integer"):
    val preference = Preference.from(3)
    assert(preference.isRight)

  test("Preference should fail with invalid integer"):
    val preference = Preference.from(0)
    assert(preference.isLeft)

  test("SummedPreference should be created from valid integer"):
    val summedPreference = SummedPreference.from(1)
    assert(summedPreference.isRight)

  test("SummedPreference should fail with invalid integer"):
    val summedPreference = SummedPreference.from(0)
    assert(summedPreference.isLeft)
