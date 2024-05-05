package pj.property

import org.scalacheck.Prop.forAll
import org.scalacheck.*
import pj.domain.SimpleTypes.{Preference, SummedPreference}

object TitleGenerators extends Properties("TitleGenerators"):

  val titleGen: Gen[String] = for {
    num <- Gen.chooseNum(1, 100)
  } yield s"Title $num"

  property("Title must be in the format 'Title X'") = forAll(titleGen) { title =>
    title.matches("Title \\d+")
  }