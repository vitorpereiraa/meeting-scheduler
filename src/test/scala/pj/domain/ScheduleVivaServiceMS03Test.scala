package pj.domain

import org.scalatest.funsuite.AnyFunSuite
import pj.domain.*
import pj.domain.DomainError.NoAvailableSlot
import pj.domain.SimpleTypes.*
import pj.domain.scheduleviva.ScheduleVivaServiceMS03

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