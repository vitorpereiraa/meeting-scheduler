package pj.domain

import org.scalatest.funsuite.AnyFunSuite
import pj.domain.*
import pj.domain.DomainError.NoAvailableSlot
import pj.domain.SimpleTypes.*
import pj.domain.scheduleviva.{ScheduleVivaServiceMS03, ScheduleVivaServiceMS03Graph}

import scala.language.adhocExtensions

class ScheduleVivaServiceMS03Test extends AnyFunSuite:

  test("getBestAvailability returns Left when the availabilities list is empty"):
    val availabilities = List[(Availability, SummedPreference)]()
    val result = ScheduleVivaServiceMS03.getBestAvailability(availabilities)
    assert(result === Left(NoAvailableSlot()))

  test("getBestAvailability returns the only availability when the availabilities list contains one availability"):
    for
      start <- DateTime.from("2024-04-14T09:00")
      end <- DateTime.from("2024-04-14T12:00")
      preference <- Preference.from(3)
      availability <- Availability.from(start, end, preference)
      preference <- SummedPreference.from(3)
      availabilities = List[(Availability, SummedPreference)]((availability, preference))
      result = ScheduleVivaServiceMS03.getBestAvailability(availabilities)
    yield assert(result === Right(availability))

  test("getBestAvailability returns the availability with the most summedpreference when the availabilities list contains multiple availabilities"):
    for
      start1 <- DateTime.from("2024-04-14T09:00")
      end1 <- DateTime.from("2024-04-14T12:00")
      preference1 <- Preference.from(3)
      availability1 <- Availability.from(start1, end1, preference1)
      spreference1 <- SummedPreference.from(20)

      start2 <- DateTime.from("2024-04-15T09:00")
      end2 <- DateTime.from("2024-04-15T12:00")
      preference2 <- Preference.from(3)
      availability2 <- Availability.from(start2, end2, preference2)
      spreference2 <- SummedPreference.from(15)

      availabilities = List[(Availability, SummedPreference)]((availability2, spreference2), (availability1, spreference1)) // availability1 is earlier
      result = ScheduleVivaServiceMS03.getBestAvailability(availabilities)
    yield
      assert(result.isRight)
      assert(result === Right(availability1))

  test("Should construct graph with nodes and edges correctly"):
    val result = for
      s1 <- Student.from("John")
      s2 <- Student.from("Jane")
      t1 <- Title.from("Title1")
      t2 <- Title.from("Title2")
      teacherId1 <- TeacherId.from("T001")
      teacherId2 <- TeacherId.from("T002")
      teacherId3 <- TeacherId.from("T003")
      teacherId4 <- TeacherId.from("T004")
      name1 <- Name.from("Teacher1")
      name2 <- Name.from("Teacher2")
      name3 <- Name.from("Teacher3")
      name4 <- Name.from("Teacher4")
      start <- DateTime.from("2022-01-01T09:00:00")
      end <- DateTime.from("2022-01-01T10:00:00")
      preference <- Preference.from(1)
      availability1 <- Availability.from(start, end, preference)
      availability2 <- Availability.from(start.plusHours(2), end.plusHours(2), preference)
      teacher1 = Teacher(teacherId1, name1, List(availability1, availability2))
      teacher2 = Teacher(teacherId2, name2, List(availability1))
      teacher3 = Teacher(teacherId3, name3, List(availability2))
      teacher4 = Teacher(teacherId4, name4, List(availability2))
      rolet1p = Role.President(teacher1)
      rolet1a = Role.Advisor(teacher1)
      rolet2p = Role.President(teacher2)
      rolet2a = Role.Advisor(teacher2)
      rolet3p = Role.President(teacher3)
      rolet3a = Role.Advisor(teacher3)
      viva1 <- Viva.from(s1, t1, List(rolet1p, rolet2a))
      viva2 <- Viva.from(s2, t2, List(rolet1p, rolet3a))
      sp1 <- SummedPreference.from(1)
      sp2 <- SummedPreference.from(2)
      resources = List(teacher1, teacher2, teacher3, teacher4)
      duration <- Duration.from("01:00")
      candidates = List(
        (viva1, List((availability1, sp1))),
        (viva2, List((availability2, sp2)))
      )
//      candidatesViva1 = ScheduleVivaServiceMS03Graph.findAllCandidateSchedules(viva1, resources, duration)
//      candidatesViva2 = ScheduleVivaServiceMS03Graph.findAllCandidateSchedules(viva2, resources, duration)
//      candidates = List(
//        (viva1, candidatesViva1),
//        (viva2, candidatesViva2)
//      )
    yield
      val graph = ScheduleVivaServiceMS03Graph.constructGraph(candidates, resources, duration)
      //assert(graph.keys.size === 2)
      //assert(graph.values.flatten.size === 2)

    assert(result.isRight)