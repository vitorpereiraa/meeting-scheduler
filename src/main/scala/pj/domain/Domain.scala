package pj.domain

import pj.domain.DomainError.*
import pj.domain.Result
import pj.domain.role.RoleService.{isAdvisor, isPresident}
import SimpleTypes.*

import scala.util.matching.Regex

final case class Agenda(duration: Duration, vivas: List[Viva], resources: List[Resource])
final case class Viva private(student: Student, title: Title, jury: List[Role])
object Viva:
  private def isValidJury(jury: List[Role]): Boolean =
    jury.exists(isPresident) &&
    jury.exists(isAdvisor)   &&
    jury.distinct.sizeIs == jury.sizeIs
  def from(student: Student, title: Title, jury: List[Role]): Result[Viva] =
    if isValidJury(jury) then
      Right(Viva(student, title, jury))
    else
      Left(InvalidJury(jury))

enum Role(val resource: Resource):
  case President(override val resource: Resource) extends Role(resource)
  case Advisor(override val resource: Resource) extends Role(resource)
  case CoAdvisor(override val resource: Resource) extends Role(resource)
  case Supervisor(override val resource: Resource) extends Role(resource)

sealed trait ResourceId:
  def value: String
object ResourceId:
  def from(value: String): Result[ResourceId] = 
    TeacherId.from(value)
      .orElse(ExternalId.from(value))
      .orElse(Left(InvalidResourceId(value)))

final case class TeacherId private(value: String) extends ResourceId
object TeacherId:
  private val pattern: Regex = """T[0-9]{3}""".r
  def from(value: String): Result[TeacherId] =
    if pattern.matches(value) then
      Right(TeacherId(value))
    else
      Left(InvalidTeacherId(value))

final case class ExternalId private(value: String) extends ResourceId
object ExternalId:
  private val pattern: Regex = """E[0-9]{3}""".r
  def from(value: String): Result[ExternalId] =
    if pattern.matches(value) then
      Right(ExternalId(value))
    else
      Left(InvalidExternalId(value))

sealed trait Resource:
  def id: ResourceId
  def name: Name
  def availability: List[Availability]
final case class Teacher(id: ResourceId, name: Name, availability: List[Availability]) extends Resource
final case class External(id: ResourceId, name: Name, availability: List[Availability]) extends Resource

final case class Availability private(start: DateTime, end: DateTime, preference: Preference)
object Availability:
  def from(start: DateTime, end: DateTime, preference: Preference): Result[Availability] =
    if end.isBefore(start) then 
      Left(InvalidAvailability("end time is before start time"))
    else
      Right(Availability(start, end, preference))

final case class ScheduledViva(student: Student, title: Title, jury: List[Role], start: DateTime, end: DateTime, preference: SummedPreference)
final case class CompleteSchedule(scheduledVivaList: List[ScheduledViva], totalPreference: SummedPreference)