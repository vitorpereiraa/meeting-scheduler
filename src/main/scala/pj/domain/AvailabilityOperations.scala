package pj.domain

import pj.domain.DomainError.*
import pj.domain.SimpleTypes.{DateTime, Duration}

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

  def updateAllAvailabilities(resources: List[Resource], viva: Viva, start: DateTime, end: DateTime): Result[List[Resource]] =
    val results = resources.map { resource =>
      if (viva.jury.exists(_.resource.id == resource.id))
        updateAvailability(resource, start, end)
      else
        Right(resource)
    }
    val (lefts, rights) = results.partitionMap(identity)
    lefts.headOption match
      case Some(error) => Left(error)
      case None => Right(rights)

  def updateAvailability(resource: Resource, start: DateTime, end: DateTime): Result[Resource] =
    val updatedAvailability = resource.availability.flatMap(availability => removeInterval(availability, start, end))
    resource match
      case Teacher(_, _, _) => Right(Teacher(resource.id, resource.name, updatedAvailability))
      case External(_, _, _) => Right(External(resource.id, resource.name, updatedAvailability))

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

  // Allen's Interval Algebra
  def precedes(a: Availability, b: Availability): Boolean =
    a.end.isBefore(b.start)

  def meets(a: Availability, b: Availability): Boolean =
    a.end.isEqual(b.start)

  def overlaps(a: Availability, b: Availability): Boolean =
    a.start.isBefore(b.start) && a.end.isAfter(b.start)

  def finishedBy(a: Availability, b: Availability): Boolean =
    a.start.isBefore(b.start) && a.end.isEqual(b.end)

  def contains(a: Availability, b: Availability): Boolean =
    a.start.isBefore(b.start) && a.end.isAfter(b.end)

  def starts(a: Availability, b: Availability): Boolean =
    a.start.isEqual(b.start) && a.end.isBefore(b.end)

  def equals(a: Availability, b: Availability): Boolean =
    a.start.isEqual(b.start) && a.end.isEqual(b.end)

  def durationOfIntersectionIsEqualOrMoreThanDuration(a: Availability, b: Availability, duration: Duration): Boolean =
    val intersection = Duration.fromBetween(a.start.max(b.start), a.end.min(b.end))
    intersection match
      case Right(i) => !i.isBefore(duration)
      case Left(l) => false

  def intersectable(a: Availability, b: Availability, duration: Duration): Boolean =
    (overlaps(a,b)    || overlaps(b, a)  ||
      finishedBy(a, b) || finishedBy(b,a) ||
      contains(a, b)   || contains(b, a)  ||
      starts(a, b)     || starts(b, a)    ||
      equals(a, b))
      && durationOfIntersectionIsEqualOrMoreThanDuration(a,b, duration)

  def intersection(a: Availability, b: Availability, duration: Duration): Availability =
    val start = a.start.max(b.start)
    val end   = a.end.min(b.end)
    Availability(start, end, a.preference)

  def intersectAvailabilityWithList(availability: Availability, list: List[Availability], duration: Duration): Option[Availability] =
    list
      .find(a1 => intersectable(availability, a1, duration))
      .map(a1 => intersection(availability, a1, duration))

  def intersectList(a: List[Availability], b: List[Availability], duration: Duration): List[Availability] =
    a.flatMap(a1 => intersectAvailabilityWithList(a1, b, duration))

  def intersectAll(a: List[List[Availability]], duration: Duration): List[Availability] = a match
    case Nil => Nil
    case head :: tail => tail.foldLeft(head)((acc, lst) => intersectList(acc, lst, duration))