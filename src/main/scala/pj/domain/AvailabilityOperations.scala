package pj.domain

import pj.domain.DomainError.ResourceInvalid

object AvailabilityOperations :

  def updateAvailability(resource: Resource, viva: ScheduledViva): Result[Resource] =
    val updatedAvailability = resource.availability.flatMap(availability => removeInterval(availability, viva))
    resource match
      case Teacher(_, _, _) => Right(Teacher(resource.id, resource.name, updatedAvailability))
      case External(_, _, _) => Right(External(resource.id, resource.name, updatedAvailability))
      case _ => Left(ResourceInvalid(resource.id))

  def removeInterval(availability: Availability, viva: ScheduledViva): List[Availability] =
    if ((viva.start.isAfter(availability.start) && viva.start.isBefore(availability.end)) || (viva.end.isAfter(availability.start) && viva.end.isBefore(availability.end)))
      val beforeViva = Availability(availability.start, viva.start, availability.preference)
      val afterViva = Availability(viva.end, availability.end, availability.preference)
      List(beforeViva, afterViva)
    else
      List(availability)


