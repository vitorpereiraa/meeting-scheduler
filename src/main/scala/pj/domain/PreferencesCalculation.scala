package pj.domain

import pj.domain.DomainError.InvalidPreference
import pj.domain.SimpleTypes.{Preference, Student, SummedPreference}

/**
 * This is a simple domain logic that sums up a list of preferences.
 * If the sum is greater than or equal to 1, it returns a `SummedPreference`.
 * Otherwise, it returns an `InvalidPreference` error.
 *
 * @param list
 * @return
 */
def sumPreferences(list: List[Preference]): Result[SummedPreference] =
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
 * @return
 */
def calculatePreferenceValue(agenda: Agenda, student: Student): Result[Preference] =
  ???
  
