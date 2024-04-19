package pj.domain.preference

import pj.domain.availability.IntervalAlgebra.*
import pj.domain.DomainError.*
import pj.domain.SimpleTypes.*
import pj.domain.*

import scala.annotation.tailrec

object PreferencesService:
  
  /**
   * This is a simple domain logic that sums up a list of preferences.
   * If the sum is greater than or equal to 1, it returns a `SummedPreference`.
   * Otherwise, it returns an `InvalidPreference` error.
   *
   * @param list
   * @return Result[SummedPreference]
   */
  def sumPreferences(list: List[Preference]): Result[SummedPreference] =
    val sum = list.foldLeft(0)((acc, pref) => acc + pref.to)
    SummedPreference.from(sum)

  def sumSummedPreferences(list: List[SummedPreference]): Result[SummedPreference] =
    val sum = list.foldLeft(0)((acc, pref) => acc + pref.to)
    SummedPreference.from(sum)

  def sumPreferencesOfScheduledVivas(scheduledVivas: List[ScheduledViva]): Result[SummedPreference] =
    val summedPreferences = scheduledVivas.map(_.preference)
    sumSummedPreferences(summedPreferences)

  def calculatePreferences(resources: List[Resource], viva: Viva, a: Availability, duration: Duration): Result[SummedPreference] =
    val myResources = resources.filter(resource => viva.jury.exists(_.resource.id == resource.id))
    @tailrec
    def loop(myResources: List[Resource], acc: List[Preference]): List[Preference] = myResources match
      case Nil => acc
      case resource :: tail =>
        val preferences = resource.availability
          .filter(a1 => intersectable(a, a1))
          .map(_.preference)
        loop(tail, acc ++ preferences)

    val preferences = loop(myResources, List.empty)
    if preferences.isEmpty then
      Left(AvailabilityNotFound(a))
    else
      sumPreferences(preferences)

  
  /**
   * Agenda contains the viva identification, a start datetime, an end datetime and a numeric schedule preference.
   * The preference of a scheduled viva is the sum of each of the numeric preferences of the resources in the
   * interval between start time and end time.
   *
   * @param agenda
   * @param student
   * @return Result[Preference]
   */
  def calculatePreferenceValuesByStudent(agenda: Agenda, student: Student, startTime: DateTime, endTime: DateTime): Result[SummedPreference] =
    val vivas = agenda.vivas.find(_.student == student);
    if vivas.isEmpty then
      Left(StudentNotFound(student.to))
    else
      // for each jury present in the viva, we get start time, end time of
      // the viva.resources.listOfAvailability between startTime and endTime
      val preferences = vivas.fold(List.empty[Preference]) { viva =>
        viva.jury.flatMap(jury =>
          jury.resource.availability.filter(avail =>
            avail.start.isBetween(startTime, endTime) && avail.end.isBetween(startTime, endTime)
          ).map(_.preference)
        )
      }
  
      if preferences.isEmpty then
        Left(AvailabilityNotFoundByStudent(student, startTime, endTime))
      else
        //sum the preferences
        SummedPreference.from(preferences.foldLeft(0)((acc, pref) => acc + pref.to))

  def calculatePreferences(agenda: Agenda, students: List[Student], startTime: DateTime, endTime: DateTime): Result[List[SummedPreference]] =
    @tailrec
    // for each student calculate the list of summed preferences
    def loop(students: List[Student], acc: List[SummedPreference]): Result[List[SummedPreference]] =
      students match
        case Nil => Right(acc)
        case student :: tail =>
          val summedPreference = PreferencesService.calculatePreferenceValuesByStudent(agenda, student, startTime, endTime)
          summedPreference match
            case Left(error) => Left(error)
            case Right(pref) => loop(tail, pref :: acc)
    loop(students, Nil)