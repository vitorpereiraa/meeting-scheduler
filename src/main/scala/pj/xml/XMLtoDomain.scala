package pj.xml

import pj.domain.*
import pj.domain.DomainError.*
import pj.domain.Role.{Advisor, President}
import pj.domain.SimpleTypes.*
import pj.domain.resource.ResourceService.findResourceById
import pj.xml.XML.*

import scala.xml.Node

object XMLtoDomain:

  def agenda(xml: Node): Result[Agenda] =
    for
      durationStr   <- fromAttribute(xml, "duration")
      duration      <- Duration.from(durationStr)
      resourcesNode <- fromNode(xml, "resources")
      resources     <- resources(resourcesNode)
      vivasNode     <- fromNode(xml, "vivas")
      vivas         <- traverse(vivasNode \ "viva", viva(resources))
    yield Agenda(duration, vivas, resources)

  private def resources(xml: Node): Result[List[Resource]] =
    for
      teachersNode  <- fromNode(xml, "teachers")
      teachers      <- traverse(teachersNode \ "teacher", teacher)
      externalsNode <- fromNode(xml, "externals")
      externals     <- traverse(externalsNode \ "external", external)
    yield teachers ::: externals

  private def teacher(xml: Node): Result[Teacher] =
    for
      teacherIdStr   <- fromAttribute(xml, "id")
      teacherId      <- TeacherId.from(teacherIdStr)
      teacherNameStr <- fromAttribute(xml, "name")
      teacherName    <- Name.from(teacherNameStr)
      availability   <- traverse(xml \ "availability", availability)
    yield Teacher(teacherId, teacherName, availability)

  private def external(xml: Node): Result[External] =
    for
      externalIdStr   <- fromAttribute(xml, "id")
      externalId      <- ExternalId.from(externalIdStr)
      externalNameStr <- fromAttribute(xml, "name")
      externalName    <- Name.from(externalNameStr)
      availability    <- traverse(xml \ "availability", availability)
    yield External(externalId, externalName, availability)

  private def availability(xml: Node): Result[Availability] =
    for
      startStr      <- fromAttribute(xml, "start")
      start         <- DateTime.from(startStr)
      endStr        <- fromAttribute(xml, "end")
      end           <- DateTime.from(endStr)
      preferenceStr <- fromAttribute(xml, "preference")
      preference    <- Preference.from(preferenceStr)
    yield Availability(start, end, preference)

  private def viva(resources: List[Resource])(xml: Node): Result[Viva] =
    for
      studentStr <- fromAttribute(xml, "student")
      student    <- Student.from(studentStr)
      titleStr   <- fromAttribute(xml, "title")
      title      <- Title.from(titleStr)
      roles      <- roles(resources)(xml)
      viva       <- Viva.from(student, title, roles)
    yield viva

  private def roles(resources: List[Resource])(xml: Node): Result[List[Role]] =
    for
      president  <- traverse(xml \ "president", role(resources)(Role.President.apply))
      advisor    <- traverse(xml \ "advisor", role(resources)(Role.Advisor.apply))
      coAdvisor  <- traverse(xml \ "coadvisor", role(resources)(Role.CoAdvisor.apply))
      supervisor <- traverse(xml \ "supervisor", role(resources)(Role.Supervisor.apply))
    yield president ::: advisor ::: coAdvisor ::: supervisor

  private def role(resources: List[Resource])(role: Resource => Role)(xml: Node): Result[Role] =
    for
      resourceIdStr <- fromAttribute(xml, "id")
      resourceId <- TeacherId.from(resourceIdStr)
        .orElse(ExternalId.from(resourceIdStr))
        .orElse(Left(InvalidResourceId(resourceIdStr)))
      resource <- findResourceById(resources)(resourceId)
    yield role(resource)
