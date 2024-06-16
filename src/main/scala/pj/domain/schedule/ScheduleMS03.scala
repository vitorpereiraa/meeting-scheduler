package pj.domain.schedule

import scala.xml.Elem
import pj.domain.*
import pj.xml.*
import pj.domain.preference.PreferencesService
import pj.domain.scheduleviva.ScheduleVivaServiceMS03


object ScheduleMS03 extends Schedule:

  def create(xml: Elem): Result[Elem] =
    for
      agenda <- XMLtoDomain.agenda(xml)
      scheduledVivas <- ScheduleVivaServiceMS03.scheduleVivaFromAgenda(agenda)
      totalPref <- PreferencesService.sumPreferencesOfScheduledVivas(scheduledVivas)
    yield DomainToXML.generateOutputXML(CompleteSchedule(scheduledVivas, totalPref))

  