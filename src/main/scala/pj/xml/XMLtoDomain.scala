package pj.xml

import pj.domain.Result
import pj.domain.schedule.SimpleTypes.*
import pj.domain.schedule.{Agenda, Jury, Role, Viva}
import pj.xml.XML.*

import scala.xml.Node

object XMLtoDomain:

  def roles(xml: Node): Result[Set[Role]] =
    ???

  def viva(xml: Node): Result[Viva] = ???
//    for
//      students  <- fromAttribute(xml, "student")
//      student   <- Student.from(students)
//      titles    <- fromAttribute(xml, "title")
//      title     <- Title.from(titles)
//      roles    <- roles(xml)
//    yield Viva.from(student, title, roles)

  def vivas(xml: Node): Result[List[Viva]] =
    traverse(xml \ "viva", viva)

  def jury(xml: Node): Result[Jury] = ???

  def jurySet(xml: Node): Result[Set[Jury]] = ???
    //traverse(xml \ "jury", jury)

  def agenda(xml: Node): Result[Agenda] =
    for
      agendaNode  <- fromNode(xml, "Agenda")
      durationStr <- fromAttribute(agendaNode, "duration")
      duration    <- Duration.from(durationStr)
      vivasNode   <- fromNode(agendaNode, "vivas")
      vivas       <- vivas(vivasNode)
      juryNode    <- fromNode(agendaNode, "jury")
      jury        <- jurySet(juryNode)
    yield Agenda(duration, vivas, jury)

