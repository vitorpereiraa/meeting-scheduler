package pj.domain

import pj.domain.*
import pj.domain.DomainError.{NoAvailableSlot, StudentNotFound}
import pj.domain.SimpleTypes.{DateTime, Duration, Preference, Student, SummedPreference}

import scala.annotation.tailrec

object ScheduleOperation:
  def isAvailable(start: DateTime, end: DateTime, availabilities: List[Availability]): Boolean =
    availabilities.exists(a => !a.start.isAfter(start) && !a.end.isBefore(end))

  def findMatchingSlots(availabilities: List[List[Availability]], duration: Duration): Result[List[Availability]] =
    availabilities.find(_.exists(availability =>
      availabilities.forall(_.exists(other => other.start.isBefore(availability.end) && other.end.isAfter(availability.start))
      ))) match
      case Some(slots) => Right(slots)
      case None => Left(NoAvailableSlot())
    
    