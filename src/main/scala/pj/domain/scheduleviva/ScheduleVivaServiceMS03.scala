package pj.domain.scheduleviva

import pj.domain.*
import pj.domain.DomainError.{NoAvailableSlot, StudentNotFound}
import pj.domain.SimpleTypes.*
import pj.domain.availability.{AvailabilityService, IntervalAlgebra}
import pj.domain.preference.PreferencesService

import scala.::
import scala.annotation.tailrec

object ScheduleVivaServiceMS03:

  def scheduleVivaFromAgenda(agenda: Agenda, maxAttempts: Int): Result[List[ScheduledViva]] =
    val (conflictingVivas, nonConflictingVivas) = partitionVivas(agenda.vivas, agenda.resources, agenda.duration)

    val initialResources = agenda.resources
    val duration = agenda.duration

    // Schedule non-conflicting vivas first
    val scheduledNonConflicting = scheduleNonConflictingVivas(nonConflictingVivas, initialResources, duration)

    // Schedule conflicting vivas with limited brute force
    val scheduledConflicting = scheduleConflictingVivas(conflictingVivas, scheduledNonConflicting._2, duration, maxAttempts)

    if (scheduledConflicting.sizeIs == conflictingVivas.size)
      Right(scheduledNonConflicting._1 ++ scheduledConflicting)
    else
      Left(NoAvailableSlot())

  /**
   * FIXME This method is not implemented correctly
   * pode estar depois a ter influência nos metodos seguintes
   *
   * @param vivas
   * @param resources
   * @param duration
   * @return
   */
  def partitionVivas(vivas: List[Viva], resources: List[Resource], duration: Duration): (List[Viva], List[Viva]) =
    val studentToVivas = vivas.groupBy(_.student)
    val (conflicting, nonConflicting) = studentToVivas.values.foldLeft((List[Viva](), List[Viva]())):
      case ((conf, nonConf), studentVivas) =>
        val hasConflicts = studentVivas.exists { viva =>
          val availabilities = getAvailabilitiesForVivas(viva, resources, duration)
          availabilities match
            case Right(availLists) =>
              val flatAvail = availLists.flatten
              flatAvail.exists(av1 => flatAvail.exists(av2 => av1 != av2 && IntervalAlgebra.overlaps(av1,av2)))
            case Left(_) => false
        }
        if (hasConflicts) (conf ++ studentVivas, nonConf)
        else (conf, nonConf ++ studentVivas)
    (conflicting.distinct, nonConflicting)

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

  def getBestAvailability(availabilities: List[Availability]): Result[Availability] =
    val sortedAvailabilities = availabilities.sortBy(a => (-a.preference.to, a.start))
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
