package pj.domain.schedule

import scala.xml.Elem
import pj.domain.*
import pj.domain.preference.PreferencesService
import pj.domain.scheduleviva.ScheduleVivaService
import pj.xml.*


object ScheduleMS01 extends Schedule:

  // TODO: Create the code to implement a functional domain model for schedule creation
  //       Use the xml.XML code to handle the xml elements
  //       Refer to https://github.com/scala/scala-xml/wiki/XML-Processing for xml creation
  def create(xml: Elem): Result[Elem] =
    for
      agenda         <- XMLtoDomain.agenda(xml)
      scheduledVivas <- ScheduleVivaService.scheduleVivaFromAgenda(agenda)
      totalPref      <- PreferencesService.sumPreferencesOfScheduledVivas(scheduledVivas)
    yield DomainToXML.generateOutputXML(CompleteSchedule(scheduledVivas, totalPref))
