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
    // Transform xml in an Agenda
    for
      agenda <- XMLtoDomain.agenda(xml)
      // First Come First Serve
      // TODO !!!
      // updateAvailability
      updateAvailability <- AvailabilityOperations.updateAvailability(???, ???)
      // calculate preferences for each student
      preferences <- PreferencesCalculation.calculatePreferences(agenda, ???, ???, ???)
      // generate complete schedule
      totalPref <- PreferencesCalculation.sumSummedPreferences(preferences)
      // generate the output file
    yield DomainToXML.generateOutputXML(???)

  def isAvailable(start: DateTime, end: DateTime, availabilities: List[Availability]): Boolean =
    availabilities.exists(a => !a.start.isAfter(start) && !a.end.isBefore(end))
    