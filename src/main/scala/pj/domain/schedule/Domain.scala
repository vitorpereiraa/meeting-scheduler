package pj.domain.schedule

import pj.domain.DomainError.InvalidJury
import pj.domain.Result
import pj.domain.schedule.RoleService.{isAdvisor, isPresident}
import pj.domain.schedule.SimpleTypes.*

final case class Agenda(duration: Duration, vivas: List[Viva], jury: Set[Jury])
final case class Viva private(student: Student, title: Title, jury: Set[Role])
object Viva:
  private def isValidJury(jury: Set[Role]): Boolean =
    jury.exists(isPresident) && jury.exists(isAdvisor)
  def from(student: Student, title: Title, jury: Set[Role]): Result[Viva] =
    if isValidJury(jury) then
      Right(Viva(student, title, jury))
    else
      Left(InvalidJury(jury))

sealed trait Role
final case class President(id: TeacherId) extends Role
final case class Advisor(id: TeacherId) extends Role
final case class CoAdvisor(id: TeacherId | ExternalId) extends Role
final case class Supervisor(id: ExternalId) extends Role

sealed trait Jury
final case class Teacher(teacherId: TeacherId, name: Name, availability: List[Availability]) extends Jury
final case class External(externalId: ExternalId, name: Name, availability: List[Availability]) extends Jury

final case class Availability(start: DateTime, end: DateTime, preference: Preference)

final case class ScheduledViva(student: Student, title: Title, start: DateTime, end: DateTime, summedPreference: SummedPreference)
final case class CompleteSchedule(scheduledVivas: List[ScheduledViva], summedPreference: SummedPreference)
