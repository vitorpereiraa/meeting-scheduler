package pj.domain

import pj.domain.*
import pj.domain.DomainError.{NoAvailableSlot, StudentNotFound}
import pj.domain.SimpleTypes.{DateTime, Duration, Preference, Student, SummedPreference}

import scala.annotation.tailrec

object ScheduleOperation:
  def isAvailable(start: DateTime, end: DateTime, availabilities: List[Availability]): Boolean =
    availabilities.exists(a => !a.start.isAfter(start) && !a.end.isBefore(end))

  def findMatchingSlotsReducedByTime(availabilities: List[List[Availability]], duration: Duration): Result[List[Availability]] = ???
//    val filteredAvailabilities = filterMatchingSlotsByDuration(availabilities, duration)    
  // para cada filteredAvailabilities quero que o start da nova availabity seja o maximo dos start possivel dentro desse periodo
  // e o end seja o start + duration. preciso tb de guardar essa preference do start
//    filteredAvailabilities match {
//      case Right(availabilityLists) =>
//        val updatedAvailabilities = availabilityLists.map { availabilityList =>
//          val sortedList = availabilityList.sortWith(_.start.isAfter(_.start))
//          sortedList.headOption.map { maxStartAvailability =>
//            Availability(maxStartAvailability.start, maxStartAvailability.start.plus(duration), maxStartAvailability.preference)
//          }
//        }.flatten
//        Right(updatedAvailabilities)
//      case Left(error) => Left(error)
//    }
    
  def filterMatchingSlotsByDuration(availabilities: List[List[Availability]], duration: Duration): Result[List[Availability]] =
    // pretendo iterar sobre a lista de availabilities e devolver uma lista de availability onde o startTime + duration esteja entre startTime e endTime
    @tailrec
    def findMatchingSlots(availabilities: List[List[Availability]], duration: Duration, acc: List[Availability]): List[Availability] =
      availabilities match
        case Nil => acc
        case availability :: tail =>
          val matchingSlots = availability.sliding(2).collect {
            case List(start, end) if !end.start.isBefore(start.start.plus(duration)) => start
          }.toList
          findMatchingSlots(tail, duration, acc ++ matchingSlots)

    val matchingSlots = findMatchingSlots(availabilities, duration, List.empty)
    if (matchingSlots.isEmpty) Left(NoAvailableSlot())
    else Right(matchingSlots)
    
            

  def getFirstAvailability(result: Result[List[Availability]]): Result[Availability] =
    result match
      case Right(availabilities) =>
        val sortedAvailabilities = availabilities.sortBy(_.start).reverse
        sortedAvailabilities.headOption match
          case Some(availability) => Right(availability)
          case None => Left(NoAvailableSlot())
      case Left(error) => Left(error)

  def getAvailabilitiesForVivas(viva: Viva, resources: List[Resource]): Result[List[List[Availability]]] =
    val availabilities = viva.jury.flatMap { role =>
      resources.find(_.id == role.resource.id).toList.flatMap(_.availability)
    }
    if (availabilities.isEmpty) Left(NoAvailableSlot())
    else Right(List(availabilities))

  /**
  def scheduleVivaFromAgenda(agenda: Agenda): Result[List[ScheduledViva]] =
    val (errors, scheduledVivas) = agenda.vivas.map { viva =>
      for {
        availabilities <- getAvailabilitiesForVivas(viva, agenda.resources)
        matchingSlots = filterMatchingSlotsByDuration(availabilities, agenda.duration)
        firstAvailability <- getFirstAvailability(matchingSlots)
        endTime = firstAvailability.start.plus(agenda.duration)
        //update the availabilities
        vivaResources = agenda.resources.filter(resource => viva.jury.exists(_.resource.id == resource.id))
        updateResult = AvailabilityOperations.updateAllAvailabilities(vivaResources, firstAvailability.start, endTime)
        _ <- updateResult match
          case (updatedResources, List()) => Right(updatedResources)
          case (_, errors) => Left(errors.head)
        summedPreferences <- PreferencesCalculation.calculatePreferences(agenda.resources, firstAvailability.start, endTime)
      } yield ScheduledViva(viva.student, viva.title, viva.jury, firstAvailability.start, endTime, summedPreferences)
    }.partitionMap(identity)
    if (scheduledVivas.isEmpty) Left(NoAvailableSlot())
    else Right(scheduledVivas)
  */

  def scheduleVivaFromAgenda(agenda: Agenda): Result[List[ScheduledViva]] =
    val initial: (List[Resource], Result[List[ScheduledViva]]) = (agenda.resources, Right(List.empty))
    val (_, result) = agenda.vivas.foldLeft(initial) { case ((resources, acc), viva) =>
      getAvailabilitiesForVivas(viva, resources) match
        case Right(availabilities) =>
          val matchingSlots = filterMatchingSlotsByDuration(availabilities, agenda.duration)
          val firstAvailability = getFirstAvailability(matchingSlots)
          firstAvailability match
            case Right(availability) =>
              val endTime = availability.start.plus(agenda.duration)
              val vivaResources = resources.filter(resource => viva.jury.exists(_.resource.id == resource.id))
              val updateResult = AvailabilityOperations.updateAllAvailabilities(vivaResources, availability.start, endTime)
              updateResult match
                case (updatedResources: List[Resource], List()) =>
                  val summedPreferences = PreferencesCalculation.calculatePreferences(updatedResources, availability.start, endTime)
                  val scheduledViva = for {
                    preferences <- summedPreferences
                  } yield ScheduledViva(viva.student, viva.title, viva.jury, availability.start, endTime, preferences)
                  (updatedResources, acc.flatMap(accVivas => scheduledViva.map(viva => accVivas :+ viva)))
                case (_, errors: List[DomainError]) => (resources, Left(errors.head))
            case Left(error) => (resources, Left(error))
        case Left(error) => (resources, Left(error))
    }
    result