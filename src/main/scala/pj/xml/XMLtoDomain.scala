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
      agendan   <- fromNode(xml, "Agenda")
      durations <- fromAttribute(agendan, "duration")
      duration  <- Duration.from(durations)
      vivasn    <- fromNode(agendan, "vivas")
      vivas     <- vivas(vivasn)
      juryn     <- fromNode(agendan, "jury")
      jury      <- jurySet(juryn)
    yield Agenda(duration, vivas, jury)

