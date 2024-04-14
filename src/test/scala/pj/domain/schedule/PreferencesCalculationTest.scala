package pj.domain.schedule

import org.scalatest.funsuite.AnyFunSuite
import pj.domain.*
import pj.domain.DomainError.InvalidPreference
import pj.domain.SimpleTypes.Preference

class PreferencesCalculationTest extends AnyFunSuite:
  test("Sum preferences - OK"):
    for
      p1 <- Preference.from(1)
      p2 <- Preference.from(2)
      p3 <- Preference.from(3)
      p4 <- Preference.from(4)
      p5 <- Preference.from(5)
      sum <- sumPreferences(List(p1, p2, p3, p4, p5))
    yield assert(sum.to == 15)

  test("Sum preferences - Invalid Preference"):
      val sum = sumPreferences(List())
      assert(Left(InvalidPreference("0")) === sum)