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

  def findEarliestAvailableSlot(availabilities: List[Availability], duration: Duration): Result[Availability] =
    @tailrec
    def loop(availabilities: List[Availability], earliestSlot: Option[Availability]): Option[Availability] =
      availabilities match
        case Nil => earliestSlot // Se a lista estiver vazia, retorna o slot mais cedo encontrado ou None se nenhum foi encontrado
        case availability :: tail =>
          // Calcula o horário final do intervalo
          val endTime = availability.start.plus(duration)

          // Verifica se o intervalo de tempo cabe na disponibilidade atual
          val isWithinAvailability = endTime.isBefore(availability.end) || endTime.equals(availability.end)

          // Se o intervalo couber na disponibilidade atual e ainda não tiver sido encontrado outro slot ou esta disponibilidade começar antes do slot mais cedo encontrado, atribui esta disponibilidade como o slot mais cedo
          if (isWithinAvailability && (earliestSlot.isEmpty || availability.start.isBefore(earliestSlot.getOrElse(availability).start)))
            loop(tail, Some(availability))
          else
            loop(tail, earliestSlot)

    // Inicia a recursão
    val earliestSlotOption = loop(availabilities, None)

    earliestSlotOption match
      case Some(slot) => Right(slot)
      case None => Left(NoAvailableSlot())

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
    for {
      availabilities <- getAvailabilitiesForVivas(viva, resources)
      matchingSlots <- filterIntersectingSlots(availabilities, duration)
      firstAvailability <- findEarliestAvailableSlot(matchingSlots, duration)
      newResources <- AvailabilityOperations.updateAllAvailabilities(resources, firstAvailability.start, firstAvailability.start.plus(duration))
      summedPreferences <- PreferencesCalculation.calculatePreferences(originalResources, firstAvailability.start, firstAvailability.start.plus(duration))
    } yield (ScheduledViva(viva.student, viva.title, viva.jury, firstAvailability.start, firstAvailability.start.plus(duration), summedPreferences),newResources)

