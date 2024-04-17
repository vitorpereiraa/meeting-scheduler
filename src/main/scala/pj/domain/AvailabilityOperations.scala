package pj.domain

import pj.domain.DomainError.*
import pj.domain.SimpleTypes.DateTime

import scala.annotation.tailrec

object AvailabilityOperations :

  @tailrec
  def updateAvailability(resources: List[Resource], start: DateTime, end: DateTime): Result[Resource] =
    resources match
      case Nil => Left(NoResourcesFound())
      case head :: tail =>
        updateAvailability(head, start, end) match
          case Right(resource) => Right(resource)
          case Left(_) => updateAvailability(tail, start, end)

  def updateAllAvailabilities(resources: List[Resource], start: DateTime, end: DateTime): (List[Resource], List[DomainError]) =
    val results = resources.map(updateAvailability(_, start, end))
    val (lefts, rights) = results.partitionMap(identity)
    (rights, lefts)

  def updateAvailability(resource: Resource, start: DateTime, end: DateTime): Result[Resource] =
    val updatedAvailability = resource.availability.flatMap(availability => removeInterval(availability, start, end))
    resource match
      case Teacher(_, _, _) => Right(Teacher(resource.id, resource.name, updatedAvailability))
      case External(_, _, _) => Right(External(resource.id, resource.name, updatedAvailability))
      case _ => Left(ResourceInvalid(resource.id))

  def removeInterval(availability: Availability, start: DateTime, end: DateTime): List[Availability] =
    if (start.isEqual(availability.start) && end.isEqual(availability.end))
      List()
    else if (start.isEqual(availability.start) && end.isBefore(availability.end))
      List(Availability(end, availability.end, availability.preference))
    else if (start.isAfter(availability.start) && end.isEqual(availability.end))
      List(Availability(availability.start, start, availability.preference))
    else if ((start.isAfter(availability.start) && start.isBefore(availability.end)) || (end.isAfter(availability.start) && end.isBefore(availability.end)))
      val beforeViva = Availability(availability.start, start, availability.preference)
      val afterViva = Availability(end, availability.end, availability.preference)
      List(beforeViva, afterViva)
    else
      List(availability)



