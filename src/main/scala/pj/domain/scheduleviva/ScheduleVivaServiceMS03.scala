package pj.domain.scheduleviva

import pj.domain.*
import pj.domain.DomainError.NoAvailableSlot
import pj.domain.SimpleTypes.*
import pj.domain.availability.AvailabilityService
import pj.domain.preference.PreferencesService

import scala.annotation.tailrec

object ScheduleVivaServiceMS03:

  def scheduleVivaFromAgenda(agenda: Agenda, maxAttempts: Int): Result[List[ScheduledViva]] =
    val (conflictingVivas, nonConflictingVivas) = partitionVivas(agenda.vivas)

    val initialResources = agenda.resources
    val duration = agenda.duration

    // Schedule non-conflicting vivas first
    val scheduledNonConflicting = scheduleNonConflictingVivas(nonConflictingVivas, initialResources, duration)

    // Schedule conflicting vivas with limited brute force
    val scheduledConflicting = scheduleConflictingVivas(conflictingVivas, scheduledNonConflicting._2, duration, maxAttempts)

    // Schedule conflicting vivas using a DAG (Directed Acyclic Graph)
    // val scheduledConflicting = ScheduleVivaServiceMS03Graph.scheduleConflictingVivas(conflictingVivas, scheduledNonConflicting._2, duration)

    if (scheduledConflicting.sizeIs == conflictingVivas.size)
      Right(scheduledNonConflicting._1 ++ scheduledConflicting)
    else
      Left(NoAvailableSlot())

  def partitionVivas(vivas: List[Viva]): (List[Viva], List[Viva]) =
    val vivasResources =
      vivas.flatMap(x => x.jury)

    val count = vivasResources.groupBy(identity).view.mapValues(_.size)

    val intersects = count.filter(x => x._2 > 1).keys.toList

    val vivasWhichResourcesIntersect =
      vivas.filter(_.jury.exists(r => intersects.contains(r)))

    (vivasWhichResourcesIntersect, vivas.diff(vivasWhichResourcesIntersect))

  def scheduleNonConflictingVivas(vivas: List[Viva], resources: List[Resource], duration: Duration): (List[ScheduledViva], List[Resource]) =
    vivas.foldLeft((List[ScheduledViva](), resources)) { case ((scheduled, res), viva) =>
      scheduleVivaFromViva(viva, res, res, duration) match
        case Right((scheduledViva, newResources)) => ((scheduledViva :: scheduled), newResources)
        case Left(_) => (scheduled, res)
    }

  def scheduleConflictingVivas(vivas: List[Viva], resources: List[Resource], duration: Duration, maxAttempts: Int): List[ScheduledViva] =
    @tailrec
    def attemptSchedule(vivas: List[Viva], resources: List[Resource], scheduled: List[ScheduledViva], attempts: Int): List[ScheduledViva] =
      if (vivas.isEmpty || attempts >= maxAttempts) scheduled
      else
        val vivaOption = vivas.headOption
        vivaOption match
          case Some(viva) =>
            scheduleVivaFromViva(viva, resources, resources, duration) match
              case Right((scheduledViva, newResources)) =>
                attemptSchedule(vivas.drop(1), newResources, (scheduledViva :: scheduled).reverse, attempts)
              case Left(_) =>
                attemptSchedule(vivas.drop(1), resources, scheduled, attempts + 1)
          case None => scheduled

    attemptSchedule(vivas, resources, List(), 0)

  def scheduleVivaFromViva(viva: Viva, resources: List[Resource], originalResources: List[Resource], duration: Duration): Result[(ScheduledViva, List[Resource])] =
    for {
      availabilities <- getAvailabilitiesForVivas(viva, resources, duration)
      intersection = AvailabilityService.intersectAll(availabilities, duration)
      firstAvailability <- getBestAvailability(intersection)
      newResources <- AvailabilityService.updateAllAvailabilities(resources, viva, firstAvailability.start, firstAvailability.start.plus(duration))
      summedPreferences <- PreferencesService.calculatePreferences(originalResources, viva, firstAvailability, duration)
    } yield (ScheduledViva(viva.student, viva.title, viva.jury, firstAvailability.start, firstAvailability.start.plus(duration), summedPreferences), newResources)

  def getBestAvailability(availabilities: List[(Availability, SummedPreference)]): Result[Availability] =
    val sortedAvailabilities = availabilities.sortBy(_._2.to)(Ordering[Int].reverse)
    sortedAvailabilities.headOption match
      case Some((availability, sp)) => Right(availability)
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
