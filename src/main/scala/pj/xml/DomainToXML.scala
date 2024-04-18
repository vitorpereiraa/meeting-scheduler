package pj.xml

import pj.domain.*
import pj.xml.XML.*

import scala.math.BigDecimal.int2bigDecimal
import scala.xml.*

object DomainToXML:

  def generateOutputXML(outputSchedule: Either[DomainError, CompleteSchedule]): Result[Elem] =
    outputSchedule match
      case Left(error) => Left(error)
      case Right(schedule) => Right(vivaListToXML(schedule))

  private def generateErrorXML(error: DomainError): Elem =
      <error xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../../scheduleError.xsd" message={error.toString}/>
  private def vivaListToXML(schedule: CompleteSchedule): Elem =
    <schedule xsi:noNamespaceSchemaLocation="../../schedule.xsd" totalPreference={schedule.totalPreference.toString} xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
      {
        schedule.scheduledVivaList.map( viva =>
          <viva student={viva.student.to} title={viva.title.to} start={viva.start.to} end={viva.end.to} preference={viva.preference.toString}>
            {
            viva.jury.flatMap {
              case Role.President(role) => Some(<president name={role.name.to}/>)
              case Role.Advisor(role) => Some(<advisor name={role.name.to}/>)
              case Role.CoAdvisor(role) => Some(<coadvisor name={role.name.to}/>)
              case Role.Supervisor(role) => Some(<supervisor name={role.name.to}/>)
            }
            }
          </viva>
        )
      }
  </schedule>