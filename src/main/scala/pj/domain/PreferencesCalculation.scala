package pj.domain

import pj.domain.DomainError.InvalidPreference
import pj.domain.SimpleTypes.{Preference, SummedPreference}

def sumPreferences(list: List[Preference]): Result[SummedPreference] =
  val sum = list.foldLeft(0)((acc, pref) => acc + pref.to)
  if sum >= 1 then
    SummedPreference.from(sum)
  else
    Left(InvalidPreference(sum.toString))
