package pj.domain

import pj.domain.*
import pj.domain.DomainError.{NoAvailableSlot, StudentNotFound}
import pj.domain.SimpleTypes.{DateTime, Duration, Preference, Student, SummedPreference}

import scala.annotation.tailrec

object ScheduleOperation:
  def isAvailable(start: DateTime, end: DateTime, availabilities: List[Availability]): Boolean =
    availabilities.exists(a => !a.start.isAfter(start) && !a.end.isBefore(end))

  def filterIntersectingSlots2(availabilities: List[List[Availability]], duration: Duration): Result[List[Availability]] =
    val intersectingSlots = availabilities.flatMap { availabilityList =>
      availabilityList
        .filter { availability =>
          val startTimePlusDuration = availability.start.plus(duration)
          startTimePlusDuration.isBefore(availability.end) || startTimePlusDuration.equals(availability.end)
        }
    }

    if (intersectingSlots.isEmpty) Left(NoAvailableSlot())
    else Right(intersectingSlots)

  def filterIntersectingSlots(availabilities: List[List[Availability]], duration: Duration): Result[List[Availability]] =
    @tailrec
    def loop(availabilities: List[List[Availability]], acc: List[Availability]): List[Availability] =
      availabilities match
        case Nil => acc
        case availability :: tail =>
          // Filter availabilities where the end time is after the calculated end time (start time + duration)
          val matchingSlots = availability.filter(avail => avail.end.isAfter(avail.start.plus(duration)))
          loop(tail, acc ++ matchingSlots)
    val matchingSlots = loop(availabilities, List.empty)
    if (matchingSlots.isEmpty) Left(NoAvailableSlot())
    else Right(matchingSlots)

  def getFirstAvailability(availabilities: List[Availability]): Result[Availability] =
    val sortedAvailabilities = availabilities.sortBy(_.start)
    sortedAvailabilities.headOption match
      case Some(availability) => Right(availability)
      case None => Left(NoAvailableSlot())

  def findEarliestAvailableSlot(availabilities: List[Availability], resources: List[Resource], duration: Duration): Result[Availability] =
    @tailrec
    def loop(availabilities: List[Availability]): Result[Availability] =
      availabilities match
        case Nil => Left(NoAvailableSlot())
        case availability :: tail =>
          val allStartTimes = generateAllStartTimes(availabilities, duration)
          val matchingSlot = allStartTimes.find { startTime =>
            val endTime = startTime.plus(duration)
            val isWithinAvailability = endTime.isBefore(availability.end) || endTime.equals(availability.end)
            val isWithinOtherResourcesAvailability = resources.forall(resource =>
              resource.availability.exists(a =>
                (a.start.isBefore(startTime) || a.start.equals(startTime)) &&
                  (a.end.isAfter(endTime) || a.end.equals(endTime))
              )
            )
            isWithinAvailability && isWithinOtherResourcesAvailability
          }
          matchingSlot match
            case Some(slot) => Right(Availability(slot, slot.plus(duration), availability.preference))
            case None => loop(tail)

    loop(availabilities)

  def getAvailabilitiesForVivas(viva: Viva, resources: List[Resource]): Result[List[List[Availability]]] =
    val availabilities = resources.filter(resource => viva.jury.exists(_.resource.id == resource.id)).flatMap(_.availability)
    if (availabilities.isEmpty) Left(NoAvailableSlot())
    else Right(List(availabilities))

  def scheduleVivaFromAgenda(agenda: Agenda): Result[List[ScheduledViva]] =
    innerScheduleVivaFromAgenda(agenda, agenda.resources).map(_.reverse)

  def innerScheduleVivaFromAgenda(agenda: Agenda, resources: List[Resource]): Result[List[ScheduledViva]] =
    val originalResources = resources
    def loop(vivas: List[Viva], resources: List[Resource], originalResources: List[Resource], acc: List[ScheduledViva]): Result[List[ScheduledViva]] = vivas match
      case Nil => Right(acc)
      case viva :: tail => scheduleVivaFromViva(viva, resources, originalResources, agenda.duration) match
        case Left(error) => Left(error)
        case Right((scheduledViva, updatedResources)) => loop(tail, updatedResources, originalResources, scheduledViva :: acc)

    loop(agenda.vivas, resources, originalResources, List.empty)

  def scheduleVivaFromViva(viva: Viva, resources: List[Resource], originalResources: List[Resource], duration: Duration): Result[(ScheduledViva, List[Resource])] =
    val vivaResources = resources.filter(resource => viva.jury.exists(_.resource.id == resource.id))
    for {
      availabilities <- getAvailabilitiesForVivas(viva, resources)
      matchingSlots <- filterIntersectingSlots(availabilities, duration)
      firstAvailability <- findEarliestAvailableSlot(matchingSlots, vivaResources, duration)
      newResources <- AvailabilityOperations.updateAllAvailabilities(resources, viva, firstAvailability.start, firstAvailability.start.plus(duration))
      summedPreferences <- PreferencesCalculation.calculatePreferences(originalResources, viva, firstAvailability.start, firstAvailability.start.plus(duration))
    } yield (ScheduledViva(viva.student, viva.title, viva.jury, firstAvailability.start, firstAvailability.start.plus(duration), summedPreferences),newResources)

  def generateAllStartTimes(availabilities: List[Availability], duration: Duration): List[DateTime] =
    availabilities.flatMap { availability =>
      Iterator.iterate(availability.start)(_.plus(duration))
        .takeWhile(time => time.plus(duration).isBefore(availability.end) || time.plus(duration).equals(availability.end))
        .toList
    }