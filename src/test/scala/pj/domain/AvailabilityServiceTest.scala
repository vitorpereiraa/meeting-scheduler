package pj.domain

import org.scalatest.funsuite.AnyFunSuite
import pj.domain.*
import pj.domain.SimpleTypes.*
import pj.domain.availability.AvailabilityService

class AvailabilityServiceTest extends AnyFunSuite:
  test("Remove Interval - Overlapping Viva"):
    for
      start <- DateTime.from("2024-04-14T09:00")
      end <- DateTime.from("2024-04-14T12:00")
      preference <- Preference.from(3)
      sumPreference <- SummedPreference.from(3)
      availability <- Availability.from(start, end, preference)
      vivaStart <- DateTime.from("2024-04-14T10:00")
      vivaEnd <- DateTime.from("2024-04-14T11:00")
      student <- Student.from("Alice")
      title <- Title.from("Thesis")
      scheduledViva = ScheduledViva(student, title, List(), vivaStart, vivaEnd, sumPreference)
      result = AvailabilityService.removeInterval(availability, vivaStart, vivaEnd)
      expectedAv1 <- Availability.from(start, vivaStart, preference)
      expectedAv2 <- Availability.from(vivaEnd, end, preference)
      expected = List(expectedAv1, expectedAv2)
    yield assert(result == expected)

  test("Remove Interval - Non-Overlapping Viva"):
    for
      start <- DateTime.from("2024-04-14T09:00")
      end <- DateTime.from("2024-04-14T12:00")
      preference <- Preference.from(3)
      sumPreference <- SummedPreference.from(3)
      availability <- Availability.from(start, end, preference)
      vivaStart <- DateTime.from("2024-04-13T13:00")
      vivaEnd <- DateTime.from("2024-04-13T14:00")
      student <- Student.from("Bob")
      title <- Title.from("Thesis")
      scheduledViva = ScheduledViva(student, title, List(), vivaStart, vivaEnd, sumPreference)
      result = AvailabilityService.removeInterval(availability, vivaStart, vivaEnd)
    yield assert(result == List(availability))

  test("Remove Interval - All Viva"):
    for
      start <- DateTime.from("2024-04-14T09:00")
      end <- DateTime.from("2024-04-14T10:00")
      preference <- Preference.from(3)
      sumPreference <- SummedPreference.from(3)
      availability <- Availability.from(start, end, preference)
      vivaStart <- DateTime.from("2024-04-14T09:00")
      vivaEnd <- DateTime.from("2024-04-14T10:00")
      student <- Student.from("Alice")
      title <- Title.from("Thesis")
      scheduledViva = ScheduledViva(student, title, List(), vivaStart, vivaEnd, sumPreference)
      result = AvailabilityService.removeInterval(availability, vivaStart, vivaEnd)
      expected = List()
    yield assert(result == expected)

  test("Remove Interval - Viva ends at the same time as Availability"):
    for
      start <- DateTime.from("2024-04-14T09:00")
      end <- DateTime.from("2024-04-14T12:00")
      preference <- Preference.from(3)
      sumPreference <- SummedPreference.from(3)
      availability <- Availability.from(start, end, preference)
      vivaStart <- DateTime.from("2024-04-14T11:00")
      vivaEnd <- DateTime.from("2024-04-14T12:00")
      student <- Student.from("Bob")
      title <- Title.from("Thesis")
      scheduledViva = ScheduledViva(student, title, List(), vivaStart, vivaEnd, sumPreference)
      result = AvailabilityService.removeInterval(availability, vivaStart, vivaEnd)
      expectedAv <- Availability.from(start, vivaStart, preference)
      expected = List(expectedAv)
    yield assert(result == expected)

  test("Update Availability - Single Resource"):
    for
      start <- DateTime.from("2024-04-14T09:00")
      end <- DateTime.from("2024-04-14T12:00")
      preference <- Preference.from(3)
      availability <- Availability.from(start, end, preference)
      resourceId <- ResourceId.from("1")
      name <- Name.from("Alice")
      resource = Teacher(resourceId, name, List(availability))
      result = AvailabilityService.updateAvailability(resource, start, end)
      expected = Right(Teacher(resourceId, name, List()))
    yield assert(result == expected)

  test("Update Availability - Multiple Resources"):
    for
      start <- DateTime.from("2024-04-14T09:00")
      end <- DateTime.from("2024-04-14T12:00")
      preference <- Preference.from(3)
      availability <- Availability.from(start, end, preference)
      resourceId1 <- ResourceId.from("1")
      resourceId2 <- ResourceId.from("2")
      name1 <- Name.from("Alice")
      name2 <- Name.from("Bob")
      resource1 = Teacher(resourceId1, name1, List(availability))
      resource2 = External(resourceId2, name2, List(availability))
      result = AvailabilityService.updateAvailability(List(resource1, resource2), start, end)
      expected = Right(resource2)
    yield assert(result == expected)

  test("Update All Availabilities"):
    for
      start <- DateTime.from("2024-04-14T09:00")
      end <- DateTime.from("2024-04-14T12:00")
      preference <- Preference.from(3)
      availability <- Availability.from(start, end, preference)
      resourceId1 <- ResourceId.from("1")
      resourceId2 <- ResourceId.from("2")
      name1 <- Name.from("Alice")
      name2 <- Name.from("Bob")
      resource1 = Teacher(resourceId1, name1, List(availability))
      resource2 = External(resourceId2, name2, List(availability))
      role1 = Role.President(resource1)
      student <- Student.from("Alice")
      title <- Title.from("Thesis")
      viva <- Viva.from(student, title, List(role1))
      result = AvailabilityService.updateAllAvailabilities(List(resource1, resource2), viva, start, end)
      expected = Right(List(Teacher(resourceId1, name1, List()), resource2))
    yield assert(result == expected)


