package pj.domain.schedule

import org.scalatest.funsuite.AnyFunSuite
import pj.domain.*
import pj.domain.DomainError.InvalidPreference
import pj.domain.SimpleTypes.{DateTime, Duration, Name, Preference, Student, Title}

class PreferencesCalculationTest extends AnyFunSuite:
  test("Sum preferences - OK"):
    for
      p1 <- Preference.from(1)
      p2 <- Preference.from(2)
      p3 <- Preference.from(3)
      p4 <- Preference.from(4)
      p5 <- Preference.from(5)
      sum <- sumPreferences(List(p1, p2, p3, p4, p5))
    yield assert(sum.to == 15)

  test("Sum preferences - Invalid Preference"):
    val sum = sumPreferences(List())
    assert(Left(InvalidPreference("0")) === sum)

  test("Calculate Preference Values By Student - OK"):
    for
      dur           <- Duration.from("01:00:00")
      student1      <- Student.from("Student 001")
      student2      <- Student.from("Student 002")
      date1         <- DateTime.from("2022-01-01T09:00:00")
      date2         <- DateTime.from("2022-01-01T10:00:00")
      date3         <- DateTime.from("2022-01-02T09:00:00")
      date4         <- DateTime.from("2022-01-02T10:00:00")
      date5         <- DateTime.from("2022-01-03T09:00:00")
      date6         <- DateTime.from("2022-01-03T10:00:00")
      pref1         <- Preference.from(1)
      pref2         <- Preference.from(2)
      pref3         <- Preference.from(3)
      availability1 = Availability(date1, date2, pref1)
      availability2 = Availability(date3, date4, pref2)
      availability3 = Availability(date5, date6, pref3)
      tid1          <- TeacherId.from("T001")
      nameT1        <- Name.from("Teacher 1")
      tid2          <- TeacherId.from("T002")
      nameT2        <- Name.from("Teacher 2")
      externalId    <- ExternalId.from("E001")
      externalName  <- Name.from("External 1")
      teacher1 = Teacher(tid1, nameT1, List(availability1))
      teacher2 = Teacher(tid2, nameT2, List(availability2, availability3))
      external1 = External(externalId, externalName, List(availability1, availability3))
      jury = List(Role.President(teacher1),Role.Advisor(teacher2), Role.Supervisor(external1))
      title         <- Title.from("Title")
      viva1         <- Viva.from(student1, title, jury)
      viva2         <- Viva.from(student2, title, jury)
      vivas = List(viva1, viva2)
      resources = List(teacher1, teacher2, external1)
      agenda = Agenda(dur, vivas, resources)
    yield 
      val calculation = calculatePreferenceValuesByStudent(agenda,student1,date1, date2)
      println(calculation)
      assert(calculation.isRight)
      assert(Right(2) === calculation)
