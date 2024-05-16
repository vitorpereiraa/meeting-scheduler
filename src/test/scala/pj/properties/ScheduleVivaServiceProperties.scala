package pj.properties

import org.scalacheck.Gen
import org.scalacheck.Properties
import org.scalacheck.Prop.forAll
import pj.properties.DomainProperties.*
import pj.properties.SimpleTypesProperties.*
import pj.domain.*
import pj.domain.scheduleviva.ScheduleVivaService

object ScheduleVivaServiceProperties extends Properties("ScheduleVivaServiceProperties"):

  def schedulableVivaGen(resources: List[Resource]): Gen[Viva] =
    ???

  def schedulableAgendaGen: Gen[Agenda] =
    ???

  property("Schedule agenda") =
    forAll(schedulableAgendaGen):
      a => ScheduleVivaService.scheduleVivaFromAgenda(a).isRight
