package pj.domain.schedule

import scala.xml.Elem
import pj.domain.*
import pj.domain.DomainError.StudentNotFound
import pj.domain.SimpleTypes.{DateTime, Duration, Preference, Student, SummedPreference}
import pj.xml.*

import scala.annotation.tailrec


object ScheduleMS01 extends Schedule:

  // TODO: Create the code to implement a functional domain model for schedule creation
  //       Use the xml.XML code to handle the xml elements
  //       Refer to https://github.com/scala/scala-xml/wiki/XML-Processing for xml creation
  def create(xml: Elem): Result[Elem] =
    XMLtoDomain.agenda(xml) match
      case Left(error) => Right(DomainToXML.generateOutputXML(Left(error)))
      case Right(agenda) =>
        println("agenda\n"+agenda)
        ScheduleOperation.scheduleVivaFromAgenda(agenda) match
          case Left(error) => Right(DomainToXML.generateOutputXML(Left(error)))
          case Right(scheduledVivas) =>
            println("scheduledVivas\n"+scheduledVivas)
            PreferencesCalculation.sumPreferencesOfScheduledVivas(scheduledVivas) match
              case Left(error) => Right(DomainToXML.generateOutputXML(Left(error)))
              case Right(totalPref) =>
                Right(DomainToXML.generateOutputXML(Right(CompleteSchedule(scheduledVivas, totalPref))))

  