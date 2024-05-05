package pj.property

import org.scalacheck.{Gen, Properties}
import org.scalacheck.Prop.forAll

object IdsAndNamesGenerators extends Properties("IdsAndNamesGenerators"):

  val teacherNameGen: Gen[(String, String)] = for {
    num <- Gen.chooseNum(1, 100)
  } yield (f"T$num%03d", f"Teacher $num%03d")

  val externalNameGen: Gen[(String, String)] = for {
    num <- Gen.chooseNum(1, 100)
  } yield (f"E$num%03d", f"External $num%03d")

  property("Teacher name must be in the format 'Teacher XXX' and ID in the format 'TXXX' and numbers must match") = forAll(teacherNameGen) { case (id, name) =>
    val idNum = id.drop(1)
    val nameNum = name.drop(7)
    id.matches("T\\d{3}") && name.matches("Teacher \\d{3}") && idNum == nameNum
  }

  property("External name must be in the format 'External XXX' and ID in the format 'EXXX' and numbers must match") = forAll(externalNameGen) { case (id, name) =>
    val idNum = id.drop(1)
    val nameNum = name.drop(8)
    id.matches("E\\d{3}") && name.matches("External \\d{3}") && idNum == nameNum
  }