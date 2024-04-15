package pj.domain

import pj.domain.DomainError.*
import pj.domain.SimpleTypes.{DateTime, Preference, Student, SummedPreference}

import scala.annotation.tailrec

object PreferencesCalculation:
  
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
    if sum >= 1 then
      SummedPreference.from(sum)
    else
      Left(InvalidPreference(sum.toString))

  def sumSummedPreferences(list: List[SummedPreference]): Result[SummedPreference] =
    val sum = list.foldLeft(0)((acc, pref) => acc + pref.to)
    if sum >= 1 then
      SummedPreference.from(sum)
    else
      Left(InvalidPreference(sum.toString))
  
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
    println("startTime: " + startTime)
    println("endTime: " + endTime)
    val vivas = agenda.vivas.find(_.student == student);
    if vivas.isEmpty then
      Left(StudentNotFound(student.to))
    else
      println(vivas)
      // for each jury present in the viva, we get start time, end time of
      // the viva.resources.listOfAvailability between startTime and endTime
      val preferences = vivas.fold(List.empty[Preference]) { viva =>
        viva.jury.flatMap(jury =>
          jury.resource.availability.filter(avail =>
            println(avail)
            DateTime.isBetween(startTime, endTime, avail.start) && DateTime.isBetween(startTime, endTime, avail.end)
          ).map(_.preference)
        )
      }
  
      if preferences.isEmpty then
        Left(AvailabilityNotFound(student, startTime, endTime))
      else
        //sum the preferences
        SummedPreference.from(preferences.foldLeft(0)((acc, pref) => acc + pref.to))
  def calculatePreferences(agenda: Agenda, students: List[Student], startTime: DateTime, endTime: DateTime): Result[List[SummedPreference]] =
    @tailrec
    // for each student calculate the list of summedpreferences
    def loop(students: List[Student], acc: List[SummedPreference]): Result[List[SummedPreference]] =
      students match
        case Nil => Right(acc)
        case student :: tail =>
          val summedPreference = PreferencesCalculation.calculatePreferenceValuesByStudent(agenda, student, startTime, endTime)
          println(summedPreference)
          summedPreference match
            case Left(error) => Left(error)
            case Right(pref) => loop(tail, pref :: acc)
    loop(students, Nil)