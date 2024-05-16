package pj.properties

import org.scalacheck.Gen
import org.scalacheck.Properties
import org.scalacheck.Prop.forAll
import pj.properties.DomainProperties.*
import pj.properties.SimpleTypesProperties.*
import pj.domain.*
import pj.domain.SimpleTypes.Duration
import pj.domain.scheduleviva.ScheduleVivaService

object ScheduleVivaServiceProperties extends Properties("ScheduleVivaServiceProperties"):

  val MIN_TEACHERS       = 2
  val MAX_TEACHERS       = 100
  val MIN_EXTERNALS      = 1
  val MAX_EXTERNALS      = 100
  val MAX_VIVAS          = 100

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
      teachersQty  <- Gen.choose(MIN_TEACHERS, MAX_TEACHERS)
      teachers     <- Gen.sequence[List[Teacher], Teacher]((0 to teachersQty).map(teacherGen))
      externalsQty <- Gen.choose(MIN_EXTERNALS, MAX_EXTERNALS)
      externals    <- Gen.sequence[List[External], External]((0 to externalsQty).map(externalGen))
    yield teachers ::: externals

  def schedulableAvailabilitiesGen(availability: Availability, duration: Duration): Gen[List[Availability]] =
    ???

  def updateResourceAvailability(role: Role, availability: Availability, duration: Duration): Gen[Role] =
    for
      availabilities <- schedulableAvailabilitiesGen(availability, duration)
    yield role match
        case Role.President(Teacher(id, name, _)) => Role.President(Teacher(id, name, availabilities))
        case Role.Advisor(Teacher(id, name, _))   => Role.Advisor(Teacher(id, name, availabilities))
        case Role.CoAdvisor(Teacher(id, name, _)) => Role.CoAdvisor(Teacher(id, name, availabilities))
        case Role.CoAdvisor(External(id, name, _)) => Role.CoAdvisor(External(id, name, availabilities))
        case Role.Supervisor(External(id, name, _)) => Role.Supervisor(External(id, name, availabilities))

  def schedulableVivaGen(jury: List[Role]):  Gen[Viva] =
    for
      student <- studentGen
      title   <- titleGen
      viva    <- Viva.from(student, title, jury).fold(_ => Gen.fail, Gen.const)
    yield viva

  def schedulableVivasGen(resources: List[Resource])(duration: Duration): Gen[List[Viva]] =
    for
      n  <- Gen.chooseNum(1, MAX_VIVAS)
      lg = (1 to n).foldLeft(Gen.const((resources, List.empty[Viva])))((gll, _) =>
        for
          (resourcesList, vivaList) <- gll
          someResources             <- Gen.someOf(resourcesList)
          jury                      <- rolesGen(someResources.toSet)
          candidateAvailability     <- availabilityGen
          juryUpdated               <- Gen.sequence[List[Role], Role](jury.map(j => updateResourceAvailability(j, candidateAvailability, duration)))
          viva                      <- schedulableVivaGen(juryUpdated)
        yield (resourcesList, vivaList.::(viva))
      )
      lv <- lg.map((_, vivaList) => vivaList)
    yield lv

  def schedulableAgendaGen: Gen[Agenda] =
    for
      duration  <- durationGen
      resources <- schedulableResourcesGen
      vivas     <- schedulableVivasGen(resources)(duration)
    yield Agenda(duration, vivas, resources)

  property("Schedule agenda") =
    forAll(schedulableAgendaGen):
      a => ScheduleVivaService.scheduleVivaFromAgenda(a).isRight
