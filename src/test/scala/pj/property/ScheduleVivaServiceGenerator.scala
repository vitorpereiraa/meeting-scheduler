package pj.property

import org.scalacheck.Gen
import org.scalacheck.Properties
import org.scalacheck.Prop.forAll
import pj.domain.scheduleviva.ScheduleVivaService.getFirstAvailability
import pj.domain.DomainError.NoAvailableSlot
import pj.property.Generators.availabilityGen
import pj.domain.Availability

object ScheduleVivaServiceGenerator extends Properties("ScheduleVivaServiceGenerator"):
  val availabilityListGen: Gen[List[Availability]] = for {
    duration <- Generators.durationGen
    availabilities <- Gen.nonEmptyListOf(Generators.availabilityGen(duration))
  } yield availabilities

  property("getFirstAvailability should return the earliest Availability if the list is not empty") =
    forAll(availabilityListGen) { availabilities =>
      val result = getFirstAvailability(availabilities)
      if (availabilities.isEmpty)
        result match
          case Left(NoAvailableSlot()) => true
          case _ => false
      else
        result match
          case Right(availability) =>
            availability == availabilities.foldLeft(Option.empty[Availability]) {
              (a: Option[Availability], b: Availability) => a match
                case Some(avail) => if (avail.start.isBefore(b.start)) Some(avail) else Some(b)
                case None => Some(b)
            }.fold(false)(identity)
          case _ => false
    }

  property("getFirstAvailability should return NoAvailableSlot if the list is empty") =
    forAll(Gen.const(List.empty[Availability])) { availabilities =>
      val result = getFirstAvailability(availabilities)
      result match
        case Left(NoAvailableSlot()) => true
        case _ => false
    }
  
  