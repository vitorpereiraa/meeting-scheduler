package pj.property

import org.scalacheck.*
import org.scalacheck.Prop.forAll
import org.scalacheck.Properties
import pj.domain.SimpleTypes.{Preference, SummedPreference}

object PreferenceGenerators extends Properties("PreferenceGenerators"):

  val MIN_LIMIT = 1
  val MAX_PREF_LIMIT = 5
  val MAX_SUM_PREF_LIMIT = 10000

  def preferenceGen: Gen[Preference] =
    for
      num <- Gen.chooseNum(MIN_LIMIT, MAX_PREF_LIMIT)
      pref <- Preference.from(num).fold(_ => Gen.fail, x => Gen.const(x))
    yield pref

  def SummedPreferenceGen: Gen[SummedPreference] =
    for
      num <- Gen.chooseNum(MIN_LIMIT, MAX_SUM_PREF_LIMIT)
      sum_pref <- SummedPreference.from(num).fold(_ => Gen.fail, x => Gen.const(x))
    yield sum_pref

  property("Preference must be between 1 and 5") =
    forAll(preferenceGen)(pref => {
      pref.to >= MIN_LIMIT && pref.to <= MAX_PREF_LIMIT
    })

  property("SummedPreference must be greater or equal than 1") =
    forAll(SummedPreferenceGen)(sum_pref => {
      sum_pref.to >= MIN_LIMIT
    })