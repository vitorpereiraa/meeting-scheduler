package pj.properties

import org.scalacheck.Prop.forAll
import org.scalacheck.*
import pj.domain.SimpleTypes.*
import pj.domain.*
import pj.domain.resource.ResourceService.{getNumOfExternals, getNumOfTeachers}
import pj.domain.role.RoleService.{isAdvisor, isPresident}
import pj.properties.SimpleTypesProperties.*

object DomainProperties extends Properties("DomainProperties"):

  val MIN_RESOURCE_ID    = 0
  val MAX_RESOURCE_ID    = 999
  val MIN_TEACHERS       = 2
  val MAX_TEACHERS       = 100
  val MIN_EXTERNALS      = 1
  val MAX_EXTERNALS      = 100
  val MIN_AVAILABILITIES = 1
  val MAX_AVAILABILITIES = 15

  // Generators
  def teacherIdGen(tid: Int): Gen[TeacherId] =
    for
      id  <- TeacherId.from("T" + "%03d".format(tid)).fold(_ => Gen.fail, Gen.const)
    yield id

  def externalIdGen(eid: Int): Gen[ExternalId] =
    for
      id  <- ExternalId.from("E" + "%03d".format(eid)).fold(_ => Gen.fail, Gen.const)
    yield id

  def availabilityGen: Gen[Availability] =
    for
      start        <- dateTimeGen
      duration     <- durationGen
      preference   <- preferenceGen
      availability <- Availability.from(start, start.plus(duration), preference).fold(_ => Gen.fail, Gen.const)
    yield availability

  def teacherGen(tid: Int): Gen[Teacher] =
    for
      id                <- teacherIdGen(tid)
      name              <- nameGen
      availabilitiesQty <- Gen.choose(MIN_AVAILABILITIES, MAX_AVAILABILITIES)
      availabilities    <- Gen.listOfN(availabilitiesQty, availabilityGen)
    yield Teacher(id, name, availabilities)

  def externalGen(eid: Int): Gen[External] =
    for
      id                <- externalIdGen(eid)
      name              <- nameGen
      availabilitiesQty <- Gen.choose(MIN_AVAILABILITIES, MAX_AVAILABILITIES)
      availabilities    <- Gen.listOfN(availabilitiesQty, availabilityGen)
    yield External(id, name, availabilities)

  def resourcesGen: Gen[List[Resource]] =
    for
      teachersQty  <- Gen.choose(MIN_TEACHERS, MAX_TEACHERS)
      teachers     <- Gen.sequence[List[Teacher], Teacher]((0 to teachersQty).map(teacherGen))
      externalsQty <- Gen.choose(MIN_EXTERNALS, MAX_EXTERNALS)
      externals    <- Gen.sequence[List[External], External]((0 to externalsQty).map(externalGen))
    yield teachers ::: externals

  def rolesGen(resources: Set[Resource]): Gen[List[Role]] =

    def findTeacher(resources: Set[Resource]): Option[Resource] =
      resources.find(r =>
        r match
          case _:Teacher  => true
          case _:External => false
      )

    def findNExternals(n: Int, resources: Set[Resource]): List[Resource] =
      resources.collect { case e: External => e }.toList.take(n)

    for
      (president, rwp) <- findTeacher(resources) match
        case Some(p)  => Gen.const((Role.President(p), resources - p))
        case None     => Gen.fail
      (advisor, rwa)  <- findTeacher(rwp) match
        case Some(a) => Gen.const((Role.Advisor(a), rwp - a))
        case None    => Gen.fail
      coAdvisorsR  <- Gen.someOf(rwa)
      coAdvisors   = coAdvisorsR.map(r => Role.CoAdvisor(r))
      nSupervisors <- Gen.choose(0, 10)
      supervisorsR = findNExternals(nSupervisors, rwa -- coAdvisorsR.toSet)
      supervisors  = supervisorsR.map(r => Role.Supervisor(r))
    yield supervisors ::: coAdvisors.toList ::: List(president) ::: List(advisor)

  def vivaGen(resources: List[Resource]): Gen[Viva] =
    for
      student <- studentGen
      title   <- titleGen
      some    <- Gen.someOf(resources)
      jury    <- rolesGen(some.toSet)
      viva    <- Viva.from(student, title, jury).fold(_ => Gen.fail, Gen.const)
    yield viva

  def agendaGen: Gen[Agenda] =
    for
      duration  <- durationGen
      resources <- resourcesGen
      vivas     <- Gen.nonEmptyListOf(vivaGen(resources))
    yield Agenda(duration, vivas, resources)

  // Properties
  property("TeacherIdGen always generates valid teacherId") =
    forAll(Gen.choose(MIN_RESOURCE_ID, MAX_RESOURCE_ID)): n =>
      forAll(teacherIdGen(n)): tid =>
        tid.value.matches("T[0-9]{3}")

  property("ExternalIdGen always generate valid externalId") =
    forAll(Gen.choose(MIN_RESOURCE_ID, MAX_RESOURCE_ID)): n =>
      forAll(externalIdGen(n)): eid =>
        eid.value.matches("E[0-9]{3}")

  property("All availabilities must be valid") =
    forAll(availabilityGen): av =>
      !av.end.isBefore(av.start)

  property("All teachers must have a id, name and at least one availability.") =
    forAll(Gen.choose(MIN_RESOURCE_ID, MAX_RESOURCE_ID)): n =>
      forAll(teacherGen(n)): t =>
          !t.name.to.isBlank &&
          !t.id.value.isBlank &&
          t.availability.nonEmpty

  property("All externals must have a id, name and at least one availability.") =
    forAll(Gen.choose(MIN_RESOURCE_ID, MAX_RESOURCE_ID)): n =>
      forAll(externalGen(n)): e =>
          !e.id.value.isBlank &&
          !e.name.to.isBlank &&
           e.availability.nonEmpty

  property("Resources are unique and always have at least two teachers") =
    forAll(resourcesGen): resources =>
      resources.sizeIs == resources.groupBy(_.id).size &&
      getNumOfTeachers(resources) >= 2 && getNumOfExternals(resources) >= 1

  property("RoleGen must generate valid jury") =
    forAll(resourcesGen): resources =>
      forAll(rolesGen(resources.toSet)): roles =>
        roles.exists(isPresident) &&
        roles.exists(isAdvisor)   &&
        roles.groupBy(_.resource.id).sizeIs == roles.size

  property("VivaGen must generate valid vivas") =
    forAll(resourcesGen): resources =>
      forAll(vivaGen(resources)): viva =>
        !viva.student.to.isBlank &&
        !viva.title.to.isBlank

  property("AgendaGen must generate valid agendas") =
    forAll(agendaGen): a =>
      a.resources.nonEmpty &&
      a.resources.sizeIs == a.resources.groupBy(_.id).size &&
      a.vivas.forall(v => v.jury.exists(isPresident) && v.jury.exists(isAdvisor)) &&
      a.vivas.forall(v => v.jury.groupBy(_.resource.id).sizeIs == v.jury.size)
