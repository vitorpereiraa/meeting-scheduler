package pj.domain

import pj.domain.SimpleTypes.*

final case class OutputSchedule(vivasSchedule: List[VivaSchedule], totalPreference: SummedPreference)

final case class VivaSchedule(student: Student, title: Title, jury: List[Role],
                              start: DateTime, end: DateTime, preference: Preference)

object VivaSchedule:

  def from(student: Student, title: Title, jury: List[Role], start: DateTime, end: DateTime, preference: Preference) =
    for
      // Viva validation - has the same validation as VivaSchedule
      viva <- Viva.from(student, title, jury)
      // add specific validation for VivaSchedule
      _ <- DateTime.isEndTimeAfterStartTime(start, end)
    yield VivaSchedule(student, title, jury, start, end, preference)
