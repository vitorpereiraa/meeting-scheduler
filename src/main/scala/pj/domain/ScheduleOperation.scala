package pj.domain

import pj.domain.*
import pj.domain.DomainError.{NoAvailableSlot, StudentNotFound}
import pj.domain.SimpleTypes.{DateTime, Duration, Preference, Student, SummedPreference}

import scala.annotation.tailrec

object ScheduleOperation:
  def isAvailable(start: DateTime, end: DateTime, availabilities: List[Availability]): Boolean =
    availabilities.exists(a => !a.start.isAfter(start) && !a.end.isBefore(end))

  def findMatchingSlots(availabilities: List[List[Availability]], duration: Duration): Result[List[Availability]] =
    val matchingSlots = for {
      availabilityList <- availabilities
      availability <- availabilityList
      if java.time.Duration.between(availability.start.toLocalDateTime, availability.end.toLocalDateTime).toMinutes >= duration.toMinutes
      if availabilities.forall(_.exists(other => other.start.isBefore(availability.end) && other.end.isAfter(availability.start)))
    } yield availability

    if (matchingSlots.isEmpty) Left(NoAvailableSlot())
    else Right(matchingSlots)

  def getFirstAvailability(result: Result[List[Availability]]): Result[Availability] =
    result match {
      case Right(availabilities) => availabilities.headOption match
        case Some(availability) => Right(availability)
        case None => Left(NoAvailableSlot())
      case Left(error) => Left(error)
    }