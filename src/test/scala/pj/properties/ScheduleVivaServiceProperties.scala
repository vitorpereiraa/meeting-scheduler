package pj.properties

import org.scalacheck.Gen
import org.scalacheck.Properties
import org.scalacheck.Prop.forAll
import pj.properties.DomainProperties.*
import pj.properties.SimpleTypesProperties.*
import pj.domain.*
import pj.domain.SimpleTypes.Duration
import pj.domain.resource.ResourceService.getNumOfTeachers
import pj.domain.scheduleviva.{ScheduleVivaService, ScheduleVivaServiceMS03}

object ScheduleVivaServiceProperties extends Properties("ScheduleVivaServiceProperties"):

  // Minimal values to reproduce the FCFS schedule problem
  val MAX_TEACHERS       = 2
  val MAX_EXTERNALS      = 1
  val MAX_VIVAS          = 10
  val MIN_AVAILABILITIES = 0
  val MAX_AVAILABILITIES = 0

  def schedulableTeacherGen(tid: Int): Gen[Teacher] =
    for
      id   <- teacherIdGen(tid)
      name <- nameGen
    yield Teacher(id, name, Nil)

  def schedulableExternalGen(eid: Int): Gen[External] =
    for
      id   <- externalIdGen(eid)
      name <- nameGen
    yield External(id, name, Nil)

  def schedulableResourcesGen: Gen[List[Resource]] =
    for
      teachersQty  <- Gen.choose(2, MAX_TEACHERS)
      teachers     <- Gen.sequence[List[Teacher], Teacher]((0 to teachersQty).map(schedulableTeacherGen))
      externalsQty <- Gen.choose(1, MAX_EXTERNALS)
      externals    <- Gen.sequence[List[External], External]((0 to externalsQty).map(schedulableExternalGen))
    yield teachers ::: externals

  def schedulableAvailabilityGen(duration: Duration): Gen[Availability] =
    for
      start        <- dateTimeGen
      preference   <- preferenceGen
      availability <- Availability.from(start, start.plus(duration), preference).fold(_ => Gen.fail, Gen.const)
    yield availability

  def schedulableAvailabilitiesGen(availability: Availability, duration: Duration): Gen[List[Availability]] =
    for
      availabilitiesQty <- Gen.choose(MIN_AVAILABILITIES, MAX_AVAILABILITIES)
      availabilities <- Gen.listOfN(availabilitiesQty, overlappingAvailabilityGen(availability, duration))
    yield availabilities.::(availability).reverse

  def overlappingAvailabilityGen(baseAvailability: Availability, duration: Duration): Gen[Availability] =
    val baseStart = baseAvailability.start
    val baseEnd = baseAvailability.end

    for
      overlapStartOffset <- Gen.choose(0L, duration.toMillis) // Random offset within the given duration
      overlapEndOffset <- Gen.choose(duration.toMillis, (baseEnd.toMillis - baseStart.toMillis) - duration.toMillis) // Ensures the overlap is at least the duration
      newStart = baseStart.plusMillis(overlapStartOffset)
      newEnd = newStart.plusMillis(overlapEndOffset)
      preference <- preferenceGen
      availability <- Availability.from(newStart, newEnd, preference).fold(_ => Gen.fail, Gen.const)
    yield availability

  def updateResourceAvailability(resource: Resource, availability: Availability, duration: Duration): Gen[Resource] =
    for
      availabilities <- schedulableAvailabilitiesGen(availability, duration)
    yield resource match
      case Teacher(id, name, a) =>  Teacher(id, name, a ::: availabilities)
      case External(id, name, a) => External(id, name, a ::: availabilities)

  def schedulableVivaGen(jury: List[Role]):  Gen[Viva] =
    for
      student <- studentGen
      title   <- titleGen
      viva    <- Viva.from(student, title, jury).fold(_ => Gen.fail, Gen.const)
    yield viva

  def mergeResources(resourceList: List[Resource], updatedResources: List[Resource]): List[Resource] =
    resourceList.map(r => updatedResources.find(_.id == r.id).fold(ifEmpty = r)(updated => updated))

  def schedulableVivasGen(resources: List[Resource])(duration: Duration): Gen[(List[Resource],List[Viva])] =
    for
      n  <- Gen.chooseNum(1, MAX_VIVAS)
      lg <- (1 to n).foldLeft(Gen.const((resources, List.empty[Viva])))((gll, _) =>
        for
          (resourcesList, vivaList) <- gll
          candidateAvailability     <- schedulableAvailabilityGen(duration)
          someResources             <- Gen.someOf(resourcesList).suchThat(r => getNumOfTeachers(r.toList) >= 2)
          updatedResources          <- Gen.sequence[List[Resource], Resource](someResources.map(r => updateResourceAvailability(r, candidateAvailability, duration)))
          jury                      <- rolesGen(updatedResources.toSet)
          viva                      <- schedulableVivaGen(jury)
        yield (mergeResources(resourcesList, updatedResources), viva :: vivaList)
      )
    yield lg

  def schedulableAgendaGen: Gen[Agenda] =
    for
      duration                  <- durationGen
      resources                 <- schedulableResourcesGen
      (resourcesUpdated, vivas) <- schedulableVivasGen(resources)(duration)
    yield Agenda(duration, vivas, resourcesUpdated)

  property("Schedule agenda - milestone 3") =
    forAll(schedulableAgendaGen):
      a => ScheduleVivaServiceMS03.scheduleVivaFromAgenda(a).isRight
      
  property("Schedule agenda - milestone 1") =
    forAll(schedulableAgendaGen):
      a => ScheduleVivaService.scheduleVivaFromAgenda(a).isRight

  