package pj.domain.availability

import pj.domain.DomainError.*
import pj.domain.SimpleTypes.{DateTime, Duration, SummedPreference}
import pj.domain.*

import scala.annotation.tailrec

object AvailabilityService :

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
      Availability.from(end, availability.end, availability.preference)
        .fold(
          _ => List(),
          availability => List(availability)
        )
    else if (start.isAfter(availability.start) && end.isEqual(availability.end))
      Availability.from(availability.start, start, availability.preference)
        .fold(
          _ => List(),
          availability => List(availability)
        )
    else if ((start.isAfter(availability.start) && start.isBefore(availability.end)) || (end.isAfter(availability.start) && end.isBefore(availability.end)))
      val beforeViva = Availability
        .from(availability.start, start, availability.preference)
      val afterViva = Availability
        .from(end, availability.end, availability.preference)
      (beforeViva, afterViva) match
        case (Right(av1),Right(av2)) => List(av1, av2)
        case _ => List()
    else
      List(availability)

  def durationOfIntersectionIsEqualOrMoreThanDuration(a: Availability, b: Availability, duration: Duration): Boolean =
    val intersection = Duration.fromBetween(a.start.max(b.start), a.end.min(b.end))
    intersection match
      case Right(i) => !i.isBefore(duration)
      case Left(l) => false

  def intersection(possibleScheduleTuple: (Availability, SummedPreference), b: Availability, duration: Duration): Option[(Availability, SummedPreference)] =
    val (a, summedPreference) = possibleScheduleTuple
    val start = a.start.max(b.start)
    val end   = a.end.min(b.end)
    val res = for {
      summedPreference <- SummedPreference.from(summedPreference.to + b.preference.to)
      intersectionAv   <- Availability.from(start, end, a.preference)
    } yield (intersectionAv, summedPreference)
    res.toOption

  def intersectAvailabilityWithList(possibleScheduleTuple: (Availability, SummedPreference), list: List[Availability], duration: Duration): Option[(Availability, SummedPreference)] =
    val (availability, summedPreference) = possibleScheduleTuple
    list
      .find(a1 => IntervalAlgebra.intersectable(availability, a1) && durationOfIntersectionIsEqualOrMoreThanDuration(availability, a1, duration))
      .flatMap(a1 => intersection(possibleScheduleTuple, a1, duration))  

  def intersectList(a: List[(Availability, SummedPreference)], b: List[Availability], duration: Duration): List[(Availability, SummedPreference)] =
    a.flatMap(a1 => intersectAvailabilityWithList(a1, b, duration))  

  def intersectAll(a: List[List[Availability]], duration: Duration): List[(Availability,SummedPreference)] = a match
    case Nil => Nil
    case head :: tail => tail.foldLeft(toPossibleScheduleTuple(head))((acc, lst) => intersectList(acc, lst, duration))
    
  def toPossibleScheduleTuple(a : List[Availability]): List[(Availability, SummedPreference)] =
    a.map(a1 => (a1, a1.preference.toSummedPreference))

  def intersectAvailabilityWithListWithoutPref(possibleScheduleTuple: (Availability), list: List[Availability], duration: Duration): Option[(Availability)] =
    val (availability) = possibleScheduleTuple
    list
      .find(a1 => IntervalAlgebra.intersectable(availability, a1) && durationOfIntersectionIsEqualOrMoreThanDuration(availability, a1, duration))
      .flatMap(a1 => intersectionWithoutPref(possibleScheduleTuple, a1, duration))

  def intersectListWithoutPref(a: List[(Availability)], b: List[Availability], duration: Duration): List[(Availability)] =
    a.flatMap(a1 => intersectAvailabilityWithListWithoutPref(a1, b, duration))
    
  def intersectAllWithoutPref(a: List[List[Availability]], duration: Duration): List[Availability] = a match
    case Nil => Nil
    case head :: tail => tail.foldLeft(head)((acc, lst) => intersectListWithoutPref(acc, lst, duration))
    
  def intersectionWithoutPref(possibleScheduleTuple: (Availability), b: Availability, duration: Duration): Option[(Availability)] =
    val (a) = possibleScheduleTuple
    val start = a.start.max(b.start)
    val end = a.end.min(b.end)
    val res = for {
      intersectionAv <- Availability.from(start, end, a.preference)
    } yield (intersectionAv)
    res.toOption