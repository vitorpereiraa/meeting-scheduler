package pj.properties

import org.scalacheck.Gen
import org.scalacheck.Properties
import org.scalacheck.Prop.forAll
import pj.properties.DomainProperties.*
import pj.properties.SimpleTypesProperties.*
import pj.domain.*
import pj.domain.SimpleTypes.Duration
import pj.domain.resource.ResourceService.getNumOfTeachers
import pj.domain.role.RoleService.{isAdvisor, isPresident}
import pj.domain.scheduleviva.ScheduleVivaService

object ScheduleVivaServiceProperties extends Properties("ScheduleVivaServiceProperties"):

  val MIN_TEACHERS       = 2
  val MAX_TEACHERS       = 4
  val MIN_EXTERNALS      = 1
  val MAX_EXTERNALS      = 4
  val MAX_VIVAS          = 4
  val MIN_AVAILABILITIES = 0
  val MAX_AVAILABILITIES = 4

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
      teachers     <- Gen.sequence[List[Teacher], Teacher]((0 to teachersQty).map(schedulableTeacherGen))
      externalsQty <- Gen.choose(MIN_EXTERNALS, MAX_EXTERNALS)
      externals    <- Gen.sequence[List[External], External]((0 to externalsQty).map(schedulableExternalGen))
    yield teachers ::: externals

  def schedulableAvailabilityGen(duration: Duration): Gen[Availability] =
    for
      start        <- dateTimeGen
      preference   <- preferenceGen
      availability <- Availability.from(start, start.plus(duration), preference).fold(_ => Gen.fail, Gen.const)
    yield availability

  /**
   * This generator must be improved.
   * The goal is to create a list of random availabilities that has the "intersection" in between.
   */
  def schedulableAvailabilitiesGen(availability: Availability, duration: Duration): Gen[List[Availability]] =
    for
      availabilitiesQty <- Gen.choose(MIN_AVAILABILITIES, MAX_AVAILABILITIES)
      availabilities    <- Gen.listOfN(availabilitiesQty, availabilityGen)
    yield availabilities.::(availability)

  def updateResourceAvailability(resource: Resource, availability: Availability, duration: Duration): Gen[Resource] =
    for
      availabilities <- schedulableAvailabilitiesGen(availability, duration)
    yield resource match
      case Teacher(id, name, _) =>  Teacher(id, name, availabilities)
      case External(id, name, _) => External(id, name, availabilities)

  //  def updateResourceAvailability(role: Role, availability: Availability, duration: Duration): Gen[Role] =
//    for
//      availabilities <- schedulableAvailabilitiesGen(availability, duration)
//    yield role match
//        case Role.President(Teacher(id, name, _)) => Role.President(Teacher(id, name, availabilities))
//        case Role.Advisor(Teacher(id, name, _))   => Role.Advisor(Teacher(id, name, availabilities))
//        case Role.CoAdvisor(Teacher(id, name, _)) => Role.CoAdvisor(Teacher(id, name, availabilities))
//        case Role.CoAdvisor(External(id, name, _)) => Role.CoAdvisor(External(id, name, availabilities))
//        case Role.Supervisor(External(id, name, _)) => Role.Supervisor(External(id, name, availabilities))
//        case _ => role
//
  def schedulableVivaGen(jury: List[Role]):  Gen[Viva] =
    for
      student <- studentGen
      title   <- titleGen
      viva    <- Viva.from(student, title, jury).fold(_ => Gen.fail, Gen.const)
    yield viva

  def mergeResources(resourceList: List[Resource], updatedResources: List[Resource]): List[Resource] =
    resourceList.map(r => updatedResources.find(_.id == r.id).fold(ifEmpty = r)(updated => updated))

  /**
   * Edge cases missing, must be improved.
   */
  def schedulableVivasGen(resources: List[Resource])(duration: Duration): Gen[(List[Resource],List[Viva])] =
    for
      n  <- Gen.chooseNum(1, MAX_VIVAS)
      lg = (1 to n).foldLeft(Gen.const((resources, List.empty[Viva])))((gll, _) =>
        for
          (resourcesList, vivaList) <- gll
          candidateAvailability     <- schedulableAvailabilityGen(duration)
//          nSome                     <- Gen.choose(2, 15).map()
//          someResources             <- Gen.listOfN(nSome, resourcesList).suchThat(l => )
//          someResources             <- Gen.choose()
          someResources             <- Gen.someOf(resourcesList).suchThat(r => getNumOfTeachers(r.toList) >= 2)
          updatedResources          <- Gen.sequence[List[Resource], Resource](someResources.map(r => updateResourceAvailability(r, candidateAvailability, duration)))
          jury                      <- rolesGen(updatedResources.toSet)
//          juryUpdated               <- Gen.sequence[List[Role], Role](jury.map(j => updateResourceAvailability(j, candidateAvailability, duration)))
          viva                      <- schedulableVivaGen(jury)
        yield (mergeResources(resourcesList, updatedResources), vivaList.::(viva))
      )
      lv <- lg//.map((resourceList, vivaList) => vivaList)
    yield lv

  def schedulableAgendaGen: Gen[Agenda] =
    for
      duration  <- durationGen
      resources <- schedulableResourcesGen
      (resourcesUpdated, vivas) <- schedulableVivasGen(resources)(duration)
    yield Agenda(duration, vivas, resourcesUpdated)

  property("Schedule agenda") =
    forAll(schedulableAgendaGen):
      a => ScheduleVivaService.scheduleVivaFromAgenda(a).isRight

//  property("SchedulableVivasGen must generate valid vivas") =
//    val sv = for
//      duration  <- durationGen
//      resources <- schedulableResourcesGen
//      (resourcesUpdated, vivas) <- schedulableVivasGen(resources)(duration)
//      agenda    = Agenda(duration, vivas , resourcesUpdated)
//      sv        <- ScheduleVivaService.scheduleVivaFromAgenda(agenda)
//    yield sv
//    sv.sample match
//      case Some(value) => println(value.isRight)
//      case None =>
    //    vivas.sample match
//      case Some(value) => value.headOption match
//        case Some(value) => println(value)// println(ScheduleVivaService.scheduleVivaFromAgenda().isRight)
//        case None => println(" no vivas")
//      case None => println(" no sample")
//    true

//    forAll(durationGen): d =>
//      forAll(schedulableResourcesGen): r =>
//        forAll(schedulableVivasGen(r)(d)): sv =>
//          println(sv)
//          true

