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
    result match
      case Right(availabilities) => availabilities.headOption match
        case Some(availability) => Right(availability)
        case None => Left(NoAvailableSlot())
      case Left(error) => Left(error)

  def getAvailabilitiesForVivas(viva: Viva, resources: List[Resource]): Result[List[List[Availability]]] =
    val availabilities = viva.jury.flatMap { role =>
      resources.find(_.id == role.resource.id).toList.flatMap(_.availability)
    }
    if (availabilities.isEmpty) Left(NoAvailableSlot())
    else Right(List(availabilities))
  
  def scheduleVivaFromAgenda(agenda: Agenda): Result[List[ScheduledViva]] =
    val (errors, scheduledVivas) = agenda.vivas.map { viva =>
      for {
        availabilities <- getAvailabilitiesForVivas(viva, agenda.resources)
        matchingSlots = findMatchingSlots(availabilities, agenda.duration)
        firstAvailability <- getFirstAvailability(matchingSlots)
        updateAvailability <- AvailabilityOperations.updateAvailability(agenda.resources, firstAvailability.start, firstAvailability.end)
        summedPreferences <- PreferencesCalculation.calculatePreferences(agenda.resources, firstAvailability.start, firstAvailability.end)
      } yield ScheduledViva(viva.student, viva.title, viva.jury, firstAvailability.start, firstAvailability.end, summedPreferences)
    }.partitionMap(identity)
    if (scheduledVivas.isEmpty) Left(NoAvailableSlot())
    else Right(scheduledVivas)
    
    