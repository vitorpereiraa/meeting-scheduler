package pj.properties

import org.scalacheck.*
import org.scalacheck.Prop.forAll
import pj.domain.*
import pj.domain.DomainError.*
import pj.domain.Role.{Advisor, President}
import pj.domain.SimpleTypes.*
import pj.domain.preference.PreferencesService.*
import pj.domain.scheduleviva.ScheduleVivaService
import pj.properties.DomainProperties.*
import pj.properties.SimpleTypesProperties.*

import scala.xml.Elem

object PreferencesServiceProperties extends Properties("PreferencesGenerators"):

  def scheduledVivaGen: Gen[ScheduledViva] =
    for
      student          <- studentGen
      title            <- titleGen
      start            <- dateTimeGen
      end              <- dateTimeGen
      summedPreference <- summedPreferenceGen
      duration         <- durationGen
      resources        <- resourcesGen
      roles            <- rolesGen(resources.toSet)
    yield ScheduledViva(student, title, roles, start, end, summedPreference)

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

