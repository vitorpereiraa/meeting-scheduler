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

  def schedulableVivasGen(resources: List[Resource])(duration: Duration): Gen[List[Viva]] =
    ???

  def schedulableAgendaGen: Gen[Agenda] =
    for
      duration  <- durationGen
      resources <- schedulableResourcesGen
      vivas     <- schedulableVivasGen(resources)(duration)
    yield Agenda(duration, vivas, resources)

  property("Schedule agenda") =
    forAll(schedulableAgendaGen):
      a => ScheduleVivaService.scheduleVivaFromAgenda(a).isRight
