package pj.domain.scheduleviva

import pj.domain.*
import pj.domain.DomainError.{NoAvailableSlot, NoResourcesFound}
import pj.domain.SimpleTypes.*
import pj.domain.availability.AvailabilityService
import pj.domain.preference.PreferencesService

import scala.annotation.tailrec

object ScheduleVivaServiceMS03:

  def scheduleVivaFromAgenda(agenda: Agenda): Result[List[ScheduledViva]] =
    val (independentVivas, commonVivas) = divideVivas(agenda.vivas)
    for
      nonConflictingVivas <- scheduleVivas(independentVivas, agenda.resources, agenda.duration)
      conflictingVivas <- scheduleVivas(commonVivas, agenda.resources, agenda.duration)
    yield (nonConflictingVivas ++ conflictingVivas).sortBy(_.start)

  def partitionVivas(vivas: List[Viva]): (List[Viva], List[Viva]) =
    val count = vivas.flatMap(x => x.jury).groupBy(identity).view.mapValues(_.size)
    val intersects = count.filter(x => x._2 > 1).keys.toList
    val vivasWhichResourcesIntersect = vivas.filter(_.jury.exists(r => intersects.contains(r)))
    (vivasWhichResourcesIntersect, vivas.diff(vivasWhichResourcesIntersect))

  def scheduleNonConflictingVivas(vivas: List[Viva], resources: List[Resource], duration: Duration): (List[ScheduledViva], List[Resource]) =
    vivas.foldLeft((List[ScheduledViva](), resources)) { case ((scheduled, res), viva) =>
      scheduleVivaFromViva(viva, res, res, duration) match
        case Right((scheduledViva, newResources)) => ((scheduledViva :: scheduled), newResources)
        case Left(_) => (scheduled, res)
    }

  def scheduleConflictingVivas(vivas: List[Viva], resources: List[Resource], duration: Duration, maxAttempts: Int): List[ScheduledViva] =
    val originalVivas: List[Viva] = vivas

    @tailrec
    def attemptSchedule(vivas: List[Viva], resources: List[Resource], scheduled: List[ScheduledViva], attempts: Int): List[ScheduledViva] =
      if (vivas.isEmpty || attempts >= maxAttempts || scheduled.sizeIs == vivas.sizeIs) scheduled
      else
        val vivaOption = vivas.headOption
        vivaOption match
          case Some(viva) =>
            scheduleVivaFromViva(viva, resources, resources, duration) match
              case Right((scheduledViva, newResources)) =>
                attemptSchedule(vivas.drop(1), newResources, (scheduledViva :: scheduled).reverse, attempts)
              case Left(_) =>
                attemptSchedule(vivas, resources, List(), 0)
          case None => scheduled

    attemptSchedule(vivas, resources, List(), 0)

  def putHeadintoLastPosition[T](list: List[T]): List[T] =
    list match
      case Nil => Nil
      case head :: tail => head :: tail.reverse

  def scheduleVivaFromViva(viva: Viva, resources: List[Resource], originalResources: List[Resource], duration: Duration): Result[(ScheduledViva, List[Resource])] =
    for
      availabilities <- getAvailabilitiesForVivas(viva, resources, duration)
      intersection = AvailabilityService.intersectAll(availabilities, duration)
      firstAvailability <- getBestAvailability(intersection)
      newResources <- AvailabilityService.updateAllAvailabilities(resources, viva, firstAvailability.start, firstAvailability.start.plus(duration))
      summedPreferences <- PreferencesService.calculatePreferences(originalResources, viva, firstAvailability, duration)
    yield (ScheduledViva(viva.student, viva.title, viva.jury, firstAvailability.start, firstAvailability.start.plus(duration), summedPreferences), newResources)

  def getBestAvailability(availabilities: List[(Availability, SummedPreference)]): Result[Availability] =
    val sortedAvailabilities = availabilities.sortBy(_._2.to)(Ordering[Int].reverse)
    sortedAvailabilities.headOption match
      case Some((availability, sp)) => Right(availability)
      case None => Left(NoAvailableSlot())

  def getAvailabilitiesForVivas(viva: Viva, resources: List[Resource], duration: Duration): Result[List[List[Availability]]] =
    val result = resources.filter(resource => viva.jury.exists(_.resource.id == resource.id))
      .map(_.availability)
      .map(availabilityList => availabilityList.filter(a => a.end.isAfter(a.start.plus(duration)) || a.end.isEqual(a.start.plus(duration))))

    if (result.isEmpty)
      Left(NoAvailableSlot())
    else
      Right(result)

  private def divideVivas(vivas: List[Viva]): (List[Viva], List[Viva]) =
    vivas.partition { viva =>
      val vivaResources = viva.jury.map(_.resource.id).toSet
      vivas.exists(other => other != viva && other.jury.exists(role => vivaResources.contains(role.resource.id)))
    }

  private def prioritizeVivas(vivas: List[Viva], resources: List[Resource]): List[Viva] =
    vivas.sortBy { viva =>
      -viva.jury.map { role =>
        resources.find(_.id == role.resource.id).map(_.availability.map(_.preference.to).sum).getOrElse(0)
      }.sum
    }

  private def scheduleVivas(vivas: List[Viva], resources: List[Resource], duration: Duration): Result[List[ScheduledViva]] =
    val prioritizedVivas = prioritizeVivas(vivas, resources)
    processAllVivasWithBestAvailability(prioritizedVivas, resources, List.empty, duration)

  @tailrec
  private def processAllVivasWithBestAvailability(vivas: List[Viva], resources: List[Resource], currentSchedule: List[ScheduledViva], duration: Duration): Either[DomainError, List[ScheduledViva]] =
    vivas match
      case Nil => Right(currentSchedule)
      case viva :: remainingVivas =>
        generateScheduleWithoutPermutation(viva, resources, duration) match
          case Some((scheduledViva, updatedResources)) => 
            processAllVivasWithBestAvailability(remainingVivas, updatedResources, scheduledViva :: currentSchedule, duration)
          case None => 
            generatePermutedSchedules(viva, resources, duration, 10) match
              case Nil => Left(NoResourcesFound()) // No feasible schedule found
              case possibleSchedules =>
                val (bestSchedule, updatedResources) = possibleSchedules.foldLeft(possibleSchedules.head):
                  case (acc, curr) if curr._1.preference.to > acc._1.preference.to => curr
                  case (acc, _) => acc
                processAllVivasWithBestAvailability(remainingVivas, updatedResources, bestSchedule :: currentSchedule, duration)

  private def generateScheduleWithoutPermutation(viva: Viva, resources: List[Resource], duration: Duration): Option[(ScheduledViva, List[Resource])] =
    val sortedAvailabilities = viva.jury.flatMap { role =>
      resources.find(_.id == role.resource.id).map(_.availability.sortBy(_.start))
    }

    if (sortedAvailabilities.size.equals(viva.jury.size))
      val intersection = AvailabilityService.intersectAllWithoutPref(sortedAvailabilities, duration)
      intersection.headOption.flatMap { interval =>
        for
          updatedResources <- AvailabilityService.updateAllAvailabilities(resources, viva, interval.start, interval.start.plus(duration)).toOption
          sumPreferences <- PreferencesService.calculatePreferences(resources, viva, interval, duration).toOption
        yield (ScheduledViva(viva.student, viva.title, viva.jury, interval.start, interval.start.plus(duration), sumPreferences), updatedResources)
      }
    else
      None

  private def generatePermutedSchedules(viva: Viva, resources: List[Resource], duration: Duration, maxPermutations: Int): List[(ScheduledViva, List[Resource])] =
    val availabilitiesByRole = viva.jury.flatMap(role => resources.find(_.id == role.resource.id).map(_.availability).getOrElse(Nil))
    val permutedAvailabilities = availabilitiesByRole.combinations(viva.jury.size).take(maxPermutations).toList
    permutedAvailabilities.flatMap { permuted =>
      val intersections = AvailabilityService.intersectAllWithoutPref(List(permuted), duration)
      intersections.flatMap { interval =>
        for
          updatedResources <- AvailabilityService.updateAllAvailabilities(resources, viva, interval.start, interval.start.plus(duration)).toOption
          sumPreferences <- PreferencesService.calculatePreferences(resources, viva, interval, duration).toOption
        yield (ScheduledViva(viva.student, viva.title, viva.jury, interval.start, interval.start.plus(duration), sumPreferences), updatedResources)
      }
    }
