package pj.domain.schedule

import scala.language.adhocExtensions
import org.scalatest.funsuite.AnyFunSuite
import pj.domain.*
import pj.domain.SimpleTypes.*
import pj.domain.schedule.ScheduleMS01

class ScheduleMS01Test extends AnyFunSuite:

  test("return true when the start and end times are within the availability") :
    for
      start <- DateTime.from("2024-04-14T09:00")
      end <- DateTime.from("2024-04-14T12:00")
      preference <- Preference.from(3)
      availability = Availability(start, end, preference)
      availabilities = List[Availability](availability)
      startFrom <- DateTime.from("2024-04-14T10:00")
      endAt <- DateTime.from("2024-04-14T11:00")
      result = ScheduleMS01.isAvailable(startFrom, endAt, availabilities)
    yield assert(result == true)

  test("return false when the start time is not within the availability") :
    for
      start <- DateTime.from("2024-04-14T09:00")
      end <- DateTime.from("2024-04-14T12:00")
      preference <- Preference.from(3)
      availability = Availability(start, end, preference)
      availabilities = List[Availability](availability)
      startFrom <- DateTime.from("2024-04-14T08:00")
      endAt <- DateTime.from("2024-04-14T13:00")
      result = ScheduleMS01.isAvailable(startFrom, endAt, availabilities)
    yield assert(result == false)

  test("return false when the end time is not within the availability") :
    for
      start <- DateTime.from("2024-04-14T09:00")
      end <- DateTime.from("2024-04-14T12:00")
      preference <- Preference.from(3)
      availability = Availability(start, end, preference)
      availabilities = List[Availability](availability)
      startFrom <- DateTime.from("2024-04-14T08:00")
      endAt <- DateTime.from("2024-04-14T11:00")
      result = ScheduleMS01.isAvailable(startFrom, endAt, availabilities)
    yield assert(result == false)

  test("return true when theres availabilities") :
    for
      start <- DateTime.from("2024-04-14T09:00")
      end <- DateTime.from("2024-04-14T12:00")
      preference <- Preference.from(3)
      availability = Availability(start, end, preference)
      availabilities = List[Availability](availability)
      result = ScheduleMS01.isAvailable(start, end, availabilities)
    yield assert(result == true)

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

      result = ScheduleMS01.isAvailable(startFrom, endAt, availabilities)
    yield assert(result == true)

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

      result = ScheduleMS01.isAvailable(startFrom, endAt, availabilities)
    yield assert(result == false)