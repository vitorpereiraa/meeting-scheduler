package pj.domain.availability

import pj.domain.Availability
import pj.domain.SimpleTypes.Duration

// Allen's Interval Algebra
object IntervalAlgebra:
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