package pj.domain.schedule

import scala.xml.Elem
import pj.domain.*
import pj.domain.DomainError.StudentNotFound
import pj.domain.SimpleTypes.{DateTime, Preference, Student, SummedPreference}
import pj.xml.*

import scala.annotation.tailrec


object ScheduleMS01 extends Schedule:

  // TODO: Create the code to implement a functional domain model for schedule creation
  //       Use the xml.XML code to handle the xml elements
  //       Refer to https://github.com/scala/scala-xml/wiki/XML-Processing for xml creation
  def create(xml: Elem): Result[Elem] =
    // Transform xml in an Agenda
    for
      agenda <- XMLtoDomain.agenda(xml)
      // calculate and update availability of resources
      // TODO !!!

      // calculate preferences for each student
      preferences <- calculatePreferences(agenda, ???, ???, ???)
      // generate complete schedule
      totalPref <- PreferencesCalculation.sumPreferences(preferences)
      // generate the output file
    yield DomainToXML.generateOutputXML(???)


  private def calculatePreferences(agenda: Agenda, students: List[Student], startTime: DateTime, endTime: DateTime): Result[List[Preference]] =
    @tailrec
    // for each student calculate the list of summedpreferences
    def loop(students: List[Student], acc: List[Preference]): Result[List[Preference]] =
      students match
        case Nil => Right(acc)
        case student :: tail =>
          val summedPreference = PreferencesCalculation.calculatePreferenceValuesByStudent(agenda, student, startTime, endTime)
          summedPreference match
            case Left(error) => Left(error)
            case Right(pref) => loop(tail, pref :: acc)
    loop(students, Nil)