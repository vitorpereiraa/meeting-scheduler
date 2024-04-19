package pj.domain

import org.scalatest.funsuite.AnyFunSuite
import pj.domain.*
import pj.domain.DomainError.NoAvailableSlot
import pj.domain.SimpleTypes.*
import pj.domain.schedule.ScheduleMS01

import scala.language.adhocExtensions

//private class ScheduleOperationTest extends AnyFunSuite:

//  test("return true when the start and end times are within the availability") :
//    for
//      start <- DateTime.from("2024-04-14T09:00")
//      end <- DateTime.from("2024-04-14T12:00")
//      preference <- Preference.from(3)
//      availability = Availability(start, end, preference)
//      availabilities = List[Availability](availability)
//      startFrom <- DateTime.from("2024-04-14T10:00")
//      endAt <- DateTime.from("2024-04-14T11:00")
//      result = ScheduleOperation.isAvailable(startFrom, endAt, availabilities)
//    yield assert(result === true)
//
//  test("return false when the start time is not within the availability") :
//    for
//      start <- DateTime.from("2024-04-14T09:00")
//      end <- DateTime.from("2024-04-14T12:00")
//      preference <- Preference.from(3)
//      availability = Availability(start, end, preference)
//      availabilities = List[Availability](availability)
//      startFrom <- DateTime.from("2024-04-14T08:00")
//      endAt <- DateTime.from("2024-04-14T13:00")
//      result = ScheduleOperation.isAvailable(startFrom, endAt, availabilities)
//    yield assert(result === false)
//
//  test("return false when the end time is not within the availability") :
//    for
//      start <- DateTime.from("2024-04-14T09:00")
//      end <- DateTime.from("2024-04-14T12:00")
//      preference <- Preference.from(3)
//      availability = Availability(start, end, preference)
//      availabilities = List[Availability](availability)
//      startFrom <- DateTime.from("2024-04-14T08:00")
//      endAt <- DateTime.from("2024-04-14T11:00")
//      result = ScheduleOperation.isAvailable(startFrom, endAt, availabilities)
//    yield assert(result === false)
//
//  test("return true when theres availabilities") :
//    for
//      start <- DateTime.from("2024-04-14T09:00")
//      end <- DateTime.from("2024-04-14T12:00")
//      preference <- Preference.from(3)
//      availability = Availability(start, end, preference)
//      availabilities = List[Availability](availability)
//      result = ScheduleOperation.isAvailable(start, end, availabilities)
//    yield assert(result === true)
//
//  test("return true when the start and end times are within one of the availabilities"):
//    for
//      start1 <- DateTime.from("2024-04-14T09:00")
//      end1 <- DateTime.from("2024-04-14T12:00")
//      preference1 <- Preference.from(3)
//      availability1 = Availability(start1, end1, preference1)
//
//      start2 <- DateTime.from("2024-04-14T13:00")
//      end2 <- DateTime.from("2024-04-14T16:00")
//      preference2 <- Preference.from(3)
//      availability2 = Availability(start2, end2, preference2)
//
//      availabilities = List[Availability](availability1, availability2)
//
//      startFrom <- DateTime.from("2024-04-14T14:00")
//      endAt <- DateTime.from("2024-04-14T15:00")
//
//      result = ScheduleOperation.isAvailable(startFrom, endAt, availabilities)
//    yield assert(result === true)
//
//  test("return false when the start and end times are not within any of the availabilities"):
//    for
//      start1 <- DateTime.from("2024-04-14T09:00")
//      end1 <- DateTime.from("2024-04-14T12:00")
//      preference1 <- Preference.from(3)
//      availability1 = Availability(start1, end1, preference1)
//
//      start2 <- DateTime.from("2024-04-14T13:00")
//      end2 <- DateTime.from("2024-04-14T16:00")
//      preference2 <- Preference.from(3)
//      availability2 = Availability(start2, end2, preference2)
//
//      availabilities = List[Availability](availability1, availability2)
//
//      startFrom <- DateTime.from("2024-04-14T12:30")
//      endAt <- DateTime.from("2024-04-14T13:30")
//
//      result = ScheduleOperation.isAvailable(startFrom, endAt, availabilities)
//    yield assert(result === false)
//
//  test("findMatchingSlots returns empty list when no common slot is available"): // FIXME: This test is not working
//      for
//        start1 <- DateTime.from("2024-04-14T09:00")
//        end1 <- DateTime.from("2024-04-14T12:00")
//        preference1 <- Preference.from(3)
//        availability1 = Availability(start1, end1, preference1)
//
//        start2 <- DateTime.from("2024-04-14T13:00")
//        end2 <- DateTime.from("2024-04-14T16:00")
//        preference2 <- Preference.from(3)
//        availability2 = Availability(start2, end2, preference2)
//
//        availabilities = List[List[Availability]](
//          List[Availability](availability1),
//          List[Availability](availability2)
//        )
//
//        duration <- Duration.from("01:00")
//        result <- ScheduleOperation.filterIntersectingSlots(availabilities, duration)
//      yield assert(result === List.empty)
//
//  test("findMatchingSlots returns the first common slot when available"):
//    for
//      start1 <- DateTime.from("2024-04-14T09:00")
//      end1 <- DateTime.from("2024-04-14T12:00")
//      preference1 <- Preference.from(3)
//      availability1 = Availability(start1, end1, preference1)
//
//      start2 <- DateTime.from("2024-04-14T10:00")
//      end2 <- DateTime.from("2024-04-14T13:00")
//      preference2 <- Preference.from(3)
//      availability2 = Availability(start2, end2, preference2)
//
//      availabilities = List[List[Availability]](
//        List[Availability](availability1),
//        List[Availability](availability2)
//      )
//
//      duration <- Duration.from("01:00")
//
//      result = ScheduleOperation.filterIntersectingSlots(availabilities, duration)
//    yield result.fold(
//      _ => false,
//      slot => slot.headOption.fold(false)(a => a.start == start2 && a.end == start2.plus(duration))
//    )
//
//  test("findMatchingSlots returns None when all slots are shorter than duration"):
//    for
//      start1 <- DateTime.from("2024-04-14T09:00")
//      end1 <- DateTime.from("2024-04-14T09:30")
//      preference1 <- Preference.from(3)
//      availability1 = Availability(start1, end1, preference1)
//
//      start2 <- DateTime.from("2024-04-14T10:00")
//      end2 <- DateTime.from("2024-04-14T10:30")
//      preference2 <- Preference.from(3)
//      availability2 = Availability(start2, end2, preference2)
//
//      availabilities = List[List[Availability]](
//        List[Availability](availability1),
//        List[Availability](availability2)
//      )
//
//      duration <- Duration.from("01:00")
//
//      result = ScheduleOperation.filterIntersectingSlots(availabilities, duration)
//    yield assert(result.isLeft)
//
//  test("findMatchingSlots returns the longest common slot when multiple are available"):
//    for
//      start1 <- DateTime.from("2024-04-14T09:00")
//      end1 <- DateTime.from("2024-04-14T12:00")
//      preference1 <- Preference.from(3)
//      availability1 = Availability(start1, end1, preference1)
//
//      start2 <- DateTime.from("2024-04-14T10:00")
//      end2 <- DateTime.from("2024-04-14T14:00")
//      preference2 <- Preference.from(3)
//      availability2 = Availability(start2, end2, preference2)
//
//      availabilities = List[List[Availability]](
//        List[Availability](availability1),
//        List[Availability](availability2)
//      )
//
//      duration <- Duration.from("01:00")
//
//      result = ScheduleOperation.filterIntersectingSlots(availabilities, duration)
//    yield result.fold(
//      _ => false,
//      slot => slot.headOption.fold(false)(a => a.start == start2 && a.end == end2)
//    )
//
//  test("getFirstAvailability returns the first availability when the result is a Right"):
//    for
//      start <- DateTime.from("2024-04-14T09:00")
//      end <- DateTime.from("2024-04-14T12:00")
//      preference <- Preference.from(3)
//      availability = Availability(start, end, preference)
//      availabilities = List[Availability](availability)
//      result = availabilities
//      firstAvailability = ScheduleOperation.getFirstAvailability(result)
//    yield assert(firstAvailability === Right(availability))
//
//  test("getFirstAvailability returns Left(NoAvailableSlot()) when the list of availabilities is empty"):
//    val result = List.empty[Availability]
//    val firstAvailability = ScheduleOperation.getFirstAvailability(result)
//    assert(firstAvailability === Left(NoAvailableSlot()))
//
//  test("getAvailabilitiesForVivas returns Left when there are no availabilities") :
//    for
//      teacherId1 <- TeacherId.from("T001")
//      teacherId2 <- TeacherId.from("T002")
//      name1 <- Name.from("Teacher1")
//      name2 <- Name.from("Teacher2")
//      resource1 = Teacher(teacherId1, name1, List())
//      resource2 = Teacher(teacherId2, name2, List())
//      role1 = Role.President(resource1)
//      role2 = Role.Advisor(resource2)
//      student <- Student.from("student")
//      title <- Title.from("Title")
//      viva <- Viva.from(student, title, List(role1, role2))
//      duration <- Duration.from("01:00")
//      result = ScheduleOperation.getAvailabilitiesForVivas(viva, List(resource1, resource2))
//    yield assert(result === Left(NoAvailableSlot()))
//
//  test("getAvailabilitiesForVivas returns Right with availabilities when there are availabilities"):
//    for
//      start <- DateTime.from("2024-04-14T09:00")
//      end <- DateTime.from("2024-04-14T12:00")
//      preference <- Preference.from(3)
//      availability = Availability(start, end, preference)
//      teacherId1 <- TeacherId.from("T001")
//      teacherId2 <- TeacherId.from("T002")
//      name1 <- Name.from("Teacher1")
//      name2 <- Name.from("Teacher2")
//      resource1 = Teacher(teacherId1, name1, List(availability))
//      resource2 = Teacher(teacherId2, name2, List(availability))
//      role1 = Role.President(resource1)
//      role2 = Role.Advisor(resource2)
//      student <- Student.from("student")
//      title <- Title.from("Title")
//      viva <- Viva.from(student, title, List(role1, role2))
//      duration <- Duration.from("01:00")
//      result = ScheduleOperation.getAvailabilitiesForVivas(viva, List(resource1, resource2))
//    yield assert(result === Right(List(List(availability, availability))))
//
//  test("scheduleVivaFromAgenda returns Right with ScheduledVivas when there are no errors") :
//    for
//      teacherId1 <- TeacherId.from("T001")
//      teacherId2 <- TeacherId.from("T002")
//      name1 <- Name.from("Teacher1")
//      name2 <- Name.from("Teacher2")
//      start <- DateTime.from("2024-04-14T09:00")
//      end <- DateTime.from("2024-04-14T12:00")
//      preference <- Preference.from(3)
//      availability = Availability(start, end, preference)
//      resource1 = Teacher(teacherId1, name1, List(availability))
//      resource2 = Teacher(teacherId2, name2, List(availability))
//
//      role1 = Role.President(resource1)
//      role2 = Role.Advisor(resource2)
//
//      student <- Student.from("student")
//      title <- Title.from("Title")
//      viva <- Viva.from(student, title, List(role1, role2))
//
//      duration <- Duration.from("01:00")
//      agenda = Agenda(duration, List(viva), List(resource1, resource2))
//
//      result = ScheduleOperation.scheduleVivaFromAgenda(agenda)
//    yield assert(result.isRight)
//
//  test("scheduleVivaFromAgenda returns Left with NoAvailableSlot when there are no available slots") :
//    for
//      teacherId1 <- TeacherId.from("T001")
//      teacherId2 <- TeacherId.from("T002")
//      name1 <- Name.from("Teacher1")
//      name2 <- Name.from("Teacher2")
//      resource1 = Teacher(teacherId1, name1, List())
//      resource2 = Teacher(teacherId2, name2, List())
//
//      role1 = Role.President(resource1)
//      role2 = Role.Advisor(resource2)
//
//      student <- Student.from("student")
//      title <- Title.from("Title")
//      viva <- Viva.from(student, title, List(role1, role2))
//
//      duration <- Duration.from("01:00")
//      agenda = Agenda(duration, List(viva), List(resource1, resource2))
//
//      result = ScheduleOperation.scheduleVivaFromAgenda(agenda)
//    yield assert(result === Left(NoAvailableSlot()))
//
//  test("scheduleVivaFromAgenda returns Left with NoAvailableSlot when all vivas have errors"):
//    for
//      teacherId1 <- TeacherId.from("T001")
//      teacherId2 <- TeacherId.from("T002")
//      name1 <- Name.from("Teacher1")
//      name2 <- Name.from("Teacher2")
//      start <- DateTime.from("2024-04-14T09:00")
//      end <- DateTime.from("2024-04-14T12:00")
//      preference <- Preference.from(3)
//      availability = Availability(start, end, preference)
//      resource1 = Teacher(teacherId1, name1, List(availability))
//      resource2 = Teacher(teacherId2, name2, List(availability))
//
//      role1 = Role.President(resource1)
//      role2 = Role.Advisor(resource2)
//
//      student <- Student.from("student")
//      title <- Title.from("Title")
//      viva1 <- Viva.from(student, title, List(role1, role2))
//      viva2 <- Viva.from(student, title, List(role1, role2))
//
//      duration <- Duration.from("05:00") // Duration longer than any availability
//      agenda = Agenda(duration, List(viva1, viva2), List(resource1, resource2))
//
//      result = ScheduleOperation.scheduleVivaFromAgenda(agenda)
//    yield assert(result === Left(NoAvailableSlot()))
//
