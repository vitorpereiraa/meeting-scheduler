package pj.domain

import org.scalatest.funsuite.AnyFunSuite
import pj.domain.*
import pj.domain.SimpleTypes.{DateTime, Name, Preference, Student, SummedPreference, Title}

class AvailabilityOperationsTest extends AnyFunSuite:
  test("Remove Interval - Overlapping Viva"):
    for
      start <- DateTime.from("2024-04-14T09:00")
      end <- DateTime.from("2024-04-14T12:00")
      preference <- Preference.from(3)
      sumPreference <- SummedPreference.from(3)
      availability = Availability(start, end, preference)
      vivaStart <- DateTime.from("2024-04-14T10:00")
      vivaEnd <- DateTime.from("2024-04-14T11:00")
      student <- Student.from("Alice")
      title <- Title.from("Thesis")
      scheduledViva = ScheduledViva(student, title, List(), vivaStart, vivaEnd, sumPreference)
      result = AvailabilityOperations.removeInterval(availability, vivaStart, vivaEnd)
      expected = List(
        Availability(start, vivaStart, preference),
        Availability(vivaEnd, end, preference)
      )
    yield assert(result == expected)

  test("Remove Interval - Non-Overlapping Viva"):
    for
      start <- DateTime.from("2024-04-14T09:00")
      end <- DateTime.from("2024-04-14T12:00")
      preference <- Preference.from(3)
      sumPreference <- SummedPreference.from(3)
      availability = Availability(start, end, preference)
      vivaStart <- DateTime.from("2024-04-13T13:00")
      vivaEnd <- DateTime.from("2024-04-13T14:00")
      student <- Student.from("Bob")
      title <- Title.from("Thesis")
      scheduledViva = ScheduledViva(student, title, List(), vivaStart, vivaEnd, sumPreference)
      result = AvailabilityOperations.removeInterval(availability, vivaStart, vivaEnd)
    yield assert(result == List(availability))

  test("Remove Interval - All Viva"):
    for
      start <- DateTime.from("2024-04-14T09:00")
      end <- DateTime.from("2024-04-14T10:00")
      preference <- Preference.from(3)
      sumPreference <- SummedPreference.from(3)
      availability = Availability(start, end, preference)
      vivaStart <- DateTime.from("2024-04-14T09:00")
      vivaEnd <- DateTime.from("2024-04-14T10:00")
      student <- Student.from("Alice")
      title <- Title.from("Thesis")
      scheduledViva = ScheduledViva(student, title, List(), vivaStart, vivaEnd, sumPreference)
      result = AvailabilityOperations.removeInterval(availability, vivaStart, vivaEnd)
      expected = List()
    yield assert(result == expected)

  test("Remove Interval - Viva ends at the same time as Availability"):
    for
      start <- DateTime.from("2024-04-14T09:00")
      end <- DateTime.from("2024-04-14T12:00")
      preference <- Preference.from(3)
      sumPreference <- SummedPreference.from(3)
      availability = Availability(start, end, preference)
      vivaStart <- DateTime.from("2024-04-14T11:00")
      vivaEnd <- DateTime.from("2024-04-14T12:00")
      student <- Student.from("Bob")
      title <- Title.from("Thesis")
      scheduledViva = ScheduledViva(student, title, List(), vivaStart, vivaEnd, sumPreference)
      result = AvailabilityOperations.removeInterval(availability, vivaStart, vivaEnd)
      expected = List(
        Availability(start, vivaStart, preference)
      )
    yield assert(result == expected)

