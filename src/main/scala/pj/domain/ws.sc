import pj.domain.*
import pj.domain.AvailabilityOperations.{intersectAll, updateAllAvailabilities}
import pj.domain.ResourceId
import pj.domain.SimpleTypes.*
import pj.domain.DomainError.*
import pj.io.FileIO
import pj.xml.XMLtoDomain
import pj.xml.DomainToXML

import scala.annotation.tailrec
import scala.xml.Utility

def getFirstAvailability(availabilities: List[Availability]): Result[Availability] =
  val sortedAvailabilities = availabilities.sortBy(_.start)
  sortedAvailabilities.headOption match
    case Some(availability) => Right(availability)
    case None => Left(NoAvailableSlot())

def getAvailabilitiesForVivas(viva: Viva, resources: List[Resource], duration: Duration): Result[List[List[Availability]]] =
  val availabilities = resources
    .filter(resource => viva.jury.exists(_.resource.id == resource.id))
    .map(_.availability)
    .map(availList =>
      availList
        .filter(a => a.end.isAfter(a.start.plus(duration)) || a.end.isEqual(a.start.plus(duration)))
    )

    if (availabilities.isEmpty)
        Left(NoAvailableSlot())
    else
        Right(availabilities)

def scheduleVivaFromViva(viva: Viva, resources: List[Resource], originalResources: List[Resource], duration: Duration): Result[(ScheduledViva, List[Resource])] =
    for {
        availabilities    <- getAvailabilitiesForVivas(viva, resources, duration)
        intersection      = intersectAll(availabilities, duration)
        firstAvailability <- getFirstAvailability(intersection)
        newResources      <- AvailabilityOperations.updateAllAvailabilities(resources, viva, firstAvailability.start, firstAvailability.start.plus(duration))
        summedPreferences <- PreferencesCalculation.calculatePreferences(originalResources, viva, firstAvailability.start, firstAvailability.start.plus(duration))
        scheduledViva = (ScheduledViva(viva.student, viva.title, viva.jury, firstAvailability.start, firstAvailability.start.plus(duration), summedPreferences), newResources)
    } yield scheduledViva

def innerScheduleVivaFromAgenda(agenda: Agenda, resources: List[Resource]): Result[List[ScheduledViva]] =
  val originalResources = resources

  def loop(vivas: List[Viva], resources: List[Resource], originalResources: List[Resource], acc: List[ScheduledViva]): Result[List[ScheduledViva]] = vivas match
    case Nil => Right(acc)
    case viva :: tail => scheduleVivaFromViva(viva, resources, originalResources, agenda.duration) match
      case Left(error) => Left(error)
      case Right((scheduledViva, updatedResources)) => loop(tail, updatedResources, originalResources, scheduledViva :: acc)

  loop(agenda.vivas, resources, originalResources, List.empty)

def scheduleVivaFromAgenda(agenda: Agenda): Result[List[ScheduledViva]] =
  innerScheduleVivaFromAgenda(agenda, agenda.resources)
    .map(_.sortWith((a,b) => a.start.isBefore(b.start)))

for
    xml            <- FileIO.load("C:\\Users\\caba\\Personal\\mei\\tap\\tap-m1a-1060503-1170541-1180511-1191244\\files\\assessment\\ms01\\valid_agenda_03_in.xml")
    agenda         <- XMLtoDomain.agenda(xml)
    scheduledVivas <- scheduleVivaFromAgenda(agenda)
//    totalPref      <- PreferencesCalculation.sumPreferencesOfScheduledVivas(scheduledVivas)
//    first     <- agenda.vivas.headOption.fold(Left(NoAvailableSlot))(r => Right(r))
//    scheduled = scheduleVivaFromViva(first, agenda.resources, agenda.resources, agenda.duration)
//    scheduledVivas <- scheduleVivaFromAgenda(agenda)
//    totalPreference  <- PreferencesCalculation.sumPreferencesOfScheduledVivas(scheduledVivas)
//    completeSchedule = CompleteSchedule(scheduledVivas, totalPreference)
//    output           = DomainToXML.generateOutputXML(completeSchedule)
//    xmlOut <- FileIO.load("C:\\Users\\caba\\Personal\\mei\\tap\\tap-m1a-1060503-1170541-1180511-1191244\\files\\assessment\\ms01\\valid_agenda_01_out.xml")
//    Utility.trim(output) == Utility.trim(xmlOut)
yield scheduledVivas.foreach(s => println(s.start.to + "  " + s.end.to + "  " + s.preference))//scheduled.foreach(println)