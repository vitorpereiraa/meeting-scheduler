package pj.property

import org.scalacheck.*
import org.scalacheck.Prop.forAll
import pj.domain.*
import pj.domain.DomainError.*
import pj.domain.Role.{Advisor, President}
import pj.domain.SimpleTypes.*
import pj.domain.preference.PreferencesService.*
import pj.domain.scheduleviva.ScheduleVivaService
import pj.property.Generators.*

import scala.xml.Elem

object PreferencesGenerators extends Properties("PreferencesGenerators"):

  property("PreferencesService.sumPreferences") =
    forAll(Gen.nonEmptyListOf(preferenceGen)) { preferences =>
      val result = sumPreferences(preferences)
      result match
        case Right(sumPref) => sumPref.to >= 1
        case Left(_) => false
    }

  property("PreferencesService.sumSummedPreferences") =
    forAll(Gen.nonEmptyListOf(summedPreferenceGen)) { summedPreferences =>
      val result = sumSummedPreferences(summedPreferences)
      result match
        case Right(summedPreference) => summedPreference.to >= 1
        case Left(_) => false
    }

  property("PreferencesService.sumPreferencesOfScheduledVivas") =
    forAll(Gen.nonEmptyListOf(scheduledVivaGen)) { scheduledVivas =>
      val result = sumPreferencesOfScheduledVivas(scheduledVivas)
      result match
        case Right(summedPreference) => summedPreference.to >= 1
        case Left(_) => false
    }

