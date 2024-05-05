package pj.property

import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop.forAll

object StudentGenerators extends Properties("StudentGenerators"):

  val studentNameGen: Gen[String] = for {
    num <- Gen.chooseNum(1, 100)
  } yield f"Student $num%03d"

  property("Student name must be in the format 'Student XXX'") = forAll(studentNameGen) { name =>
    name.matches("Student \\d{3}")
  }