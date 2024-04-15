package pj.domain.schedule

import scala.language.adhocExtensions
import org.scalatest.funsuite.AnyFunSuite
import pj.domain.*
import pj.domain.SimpleTypes.*
import pj.domain.schedule.ScheduleMS01

private class ScheduleOperationTest extends AnyFunSuite:

  test("return true when the start and end times are within the availability") :
    for
      start <- DateTime.from("2024-04-14T09:00")
      end <- DateTime.from("2024-04-14T12:00")
      preference <- Preference.from(3)
      availability = Availability(start, end, preference)
      availabilities = List[Availability](availability)
      startFrom <- DateTime.from("2024-04-14T10:00")
      endAt <- DateTime.from("2024-04-14T11:00")
      result = ScheduleOperation.isAvailable(startFrom, endAt, availabilities)
    yield assert(result === true)

  test("return false when the start time is not within the availability") :
    for
      start <- DateTime.from("2024-04-14T09:00")
      end <- DateTime.from("2024-04-14T12:00")
      preference <- Preference.from(3)
      availability = Availability(start, end, preference)
      availabilities = List[Availability](availability)
      startFrom <- DateTime.from("2024-04-14T08:00")
      endAt <- DateTime.from("2024-04-14T13:00")
      result = ScheduleOperation.isAvailable(startFrom, endAt, availabilities)
    yield assert(result === false)

  test("return false when the end time is not within the availability") :
    for
      start <- DateTime.from("2024-04-14T09:00")
      end <- DateTime.from("2024-04-14T12:00")
      preference <- Preference.from(3)
      availability = Availability(start, end, preference)
      availabilities = List[Availability](availability)
      startFrom <- DateTime.from("2024-04-14T08:00")
      endAt <- DateTime.from("2024-04-14T11:00")
      result = ScheduleOperation.isAvailable(startFrom, endAt, availabilities)
    yield assert(result === false)

  test("return true when theres availabilities") :
    for
      start <- DateTime.from("2024-04-14T09:00")
      end <- DateTime.from("2024-04-14T12:00")
      preference <- Preference.from(3)
      availability = Availability(start, end, preference)
      availabilities = List[Availability](availability)
      result = ScheduleOperation.isAvailable(start, end, availabilities)
    yield assert(result === true)

  test("return true when the start and end times are within one of the availabilities"):
    for
      start1 <- DateTime.from("2024-04-14T09:00")
      end1 <- DateTime.from("2024-04-14T12:00")
      preference1 <- Preference.from(3)
      availability1 = Availability(start1, end1, preference1)

      start2 <- DateTime.from("2024-04-14T13:00")
      end2 <- DateTime.from("2024-04-14T16:00")
      preference2 <- Preference.from(3)
      availability2 = Availability(start2, end2, preference2)

      availabilities = List[Availability](availability1, availability2)

      startFrom <- DateTime.from("2024-04-14T14:00")
      endAt <- DateTime.from("2024-04-14T15:00")

      result = ScheduleOperation.isAvailable(startFrom, endAt, availabilities)
    yield assert(result === true)

  test("return false when the start and end times are not within any of the availabilities"):
    for
      start1 <- DateTime.from("2024-04-14T09:00")
      end1 <- DateTime.from("2024-04-14T12:00")
      preference1 <- Preference.from(3)
      availability1 = Availability(start1, end1, preference1)

      start2 <- DateTime.from("2024-04-14T13:00")
      end2 <- DateTime.from("2024-04-14T16:00")
      preference2 <- Preference.from(3)
      availability2 = Availability(start2, end2, preference2)

      availabilities = List[Availability](availability1, availability2)

      startFrom <- DateTime.from("2024-04-14T12:30")
      endAt <- DateTime.from("2024-04-14T13:30")

      result = ScheduleOperation.isAvailable(startFrom, endAt, availabilities)
    yield assert(result === false)

  test("findMatchingSlots returns None when no common slot is available"):
    for
      start1 <- DateTime.from("2024-04-14T09:00")
      end1 <- DateTime.from("2024-04-14T12:00")
      preference1 <- Preference.from(3)
      availability1 = Availability(start1, end1, preference1)

      start2 <- DateTime.from("2024-04-14T13:00")
      end2 <- DateTime.from("2024-04-14T16:00")
      preference2 <- Preference.from(3)
      availability2 = Availability(start2, end2, preference2)

      availabilities = List[List[Availability]](
        List[Availability](availability1),
        List[Availability](availability2)
      )

      duration <- Duration.from("01:00")

      result = ScheduleOperation.findMatchingSlots(availabilities, duration)
    yield assert(result.isLeft)

  test("findMatchingSlots returns the first common slot when available"):
    for
      start1 <- DateTime.from("2024-04-14T09:00")
      end1 <- DateTime.from("2024-04-14T12:00")
      preference1 <- Preference.from(3)
      availability1 = Availability(start1, end1, preference1)

      start2 <- DateTime.from("2024-04-14T10:00")
      end2 <- DateTime.from("2024-04-14T13:00")
      preference2 <- Preference.from(3)
      availability2 = Availability(start2, end2, preference2)

      availabilities = List[List[Availability]](
        List[Availability](availability1),
        List[Availability](availability2)
      )

      duration <- Duration.from("01:00")

      result = ScheduleOperation.findMatchingSlots(availabilities, duration)
    yield result.fold(
      _ => false,
      slot => slot.headOption.fold(false)(a => a.start == start2 && a.end == start2.plus(duration))
    )

  test("findMatchingSlots returns None when all slots are shorter than duration"):
    for
      start1 <- DateTime.from("2024-04-14T09:00")
      end1 <- DateTime.from("2024-04-14T09:30")
      preference1 <- Preference.from(3)
      availability1 = Availability(start1, end1, preference1)

      start2 <- DateTime.from("2024-04-14T10:00")
      end2 <- DateTime.from("2024-04-14T10:30")
      preference2 <- Preference.from(3)
      availability2 = Availability(start2, end2, preference2)

      availabilities = List[List[Availability]](
        List[Availability](availability1),
        List[Availability](availability2)
      )

      duration <- Duration.from("01:00")

      result = ScheduleOperation.findMatchingSlots(availabilities, duration)
    yield assert(result.isLeft)

  test("findMatchingSlots returns the longest common slot when multiple are available"):
    for
      start1 <- DateTime.from("2024-04-14T09:00")
      end1 <- DateTime.from("2024-04-14T12:00")
      preference1 <- Preference.from(3)
      availability1 = Availability(start1, end1, preference1)

      start2 <- DateTime.from("2024-04-14T10:00")
      end2 <- DateTime.from("2024-04-14T14:00")
      preference2 <- Preference.from(3)
      availability2 = Availability(start2, end2, preference2)

      availabilities = List[List[Availability]](
        List[Availability](availability1),
        List[Availability](availability2)
      )

      duration <- Duration.from("01:00")

      result = ScheduleOperation.findMatchingSlots(availabilities, duration)
    yield result.fold(
      _ => false,
      slot => slot.headOption.fold(false)(a => a.start == start2 && a.end == end2)
    )