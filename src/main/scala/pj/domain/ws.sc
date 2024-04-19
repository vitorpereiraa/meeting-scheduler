import pj.domain.{PreferencesCalculation, ScheduleOperation}
import pj.io.FileIO
import pj.xml.XMLtoDomain

for
    xml            <- FileIO.load("C:\\Users\\caba\\Personal\\mei\\tap\\tap-m1a-1060503-1170541-1180511-1191244\\files\\assessment\\ms01\\valid_agenda_27_in.xml")
    agenda         <- XMLtoDomain.agenda(xml)
    scheduledVivas <- ScheduleOperation.scheduleVivaFromAgenda(agenda)
//    totalPref      <- PreferencesCalculation.sumPreferencesOfScheduledVivas(scheduledVivas)
//    completeSchedule = CompleteSchedule(scheduledVivas, totalPreference)
//    output           = DomainToXML.generateOutputXML(completeSchedule)
//    xmlOut <- FileIO.load("C:\\Users\\caba\\Personal\\mei\\tap\\tap-m1a-1060503-1170541-1180511-1191244\\files\\assessment\\ms01\\valid_agenda_01_out.xml")
//    Utility.trim(output) == Utility.trim(xmlOut)
yield scheduledVivas.foreach(s => println(s.student.to + " " + s.start.to + "  " + s.end.to + "  " + s.preference))