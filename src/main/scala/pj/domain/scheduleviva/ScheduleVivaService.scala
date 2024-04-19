package pj.domain.scheduleviva

import pj.domain.*
import pj.domain.DomainError.{NoAvailableSlot, StudentNotFound}
import pj.domain.SimpleTypes.*
import pj.domain.availability.AvailabilityService
import pj.domain.preference.PreferencesService

import scala.annotation.tailrec

object ScheduleVivaService:

  def getFirstAvailability(availabilities: List[Availability]): Result[Availability] =
    val sortedAvailabilities = availabilities.sortBy(_.start)
    sortedAvailabilities.headOption match
      case Some(availability) => Right(availability)
      case None => Left(NoAvailableSlot())

  def getAvailabilitiesForVivas(viva: Viva, resources: List[Resource], duration: Duration): Result[List[List[Availability]]] =
    val availabilities = resources
      .filter(resource => viva.jury.exists(_.resource.id == resource.id))
      .map(_.availability)
      .map(availList =>
        availList
          .filter(a => a.end.isAfter(a.start.plus(duration)) || a.end.isEqual(a.start.plus(duration)))
      )

    if (availabilities.isEmpty)
      Left(NoAvailableSlot())
    else
      Right(availabilities)

  def scheduleVivaFromViva(viva: Viva, resources: List[Resource], originalResources: List[Resource], duration: Duration): Result[(ScheduledViva, List[Resource])] =
    for {
      availabilities    <- getAvailabilitiesForVivas(viva, resources, duration)
      intersection      = AvailabilityService.intersectAll(availabilities, duration)
      firstAvailability <- getFirstAvailability(intersection)
      newResources      <- AvailabilityService.updateAllAvailabilities(resources, viva, firstAvailability.start, firstAvailability.start.plus(duration))
      summedPreferences <- PreferencesService.calculatePreferences(originalResources, viva, firstAvailability, duration)
    } yield (ScheduledViva(viva.student, viva.title, viva.jury, firstAvailability.start, firstAvailability.start.plus(duration), summedPreferences), newResources)

  def innerScheduleVivaFromAgenda(agenda: Agenda, resources: List[Resource]): Result[List[ScheduledViva]] =
    val originalResources = resources
    @tailrec
    def loop(vivas: List[Viva], resources: List[Resource], originalResources: List[Resource], acc: List[ScheduledViva]): Result[List[ScheduledViva]] = vivas match
      case Nil => Right(acc)
      case viva :: tail => scheduleVivaFromViva(viva, resources, originalResources, agenda.duration) match
        case Left(error) => Left(error)
        case Right((scheduledViva, updatedResources)) => loop(tail, updatedResources, originalResources, scheduledViva :: acc)
    loop(agenda.vivas, resources, originalResources, List.empty)

  def scheduleVivaFromAgenda(agenda: Agenda): Result[List[ScheduledViva]] =
    innerScheduleVivaFromAgenda(agenda, agenda.resources)
      .map(_.sortWith((a, b) => if !a.start.isEqual(b.start) then a.start.isBefore(b.start) else a.student.compareTo(b.student) < 0))
