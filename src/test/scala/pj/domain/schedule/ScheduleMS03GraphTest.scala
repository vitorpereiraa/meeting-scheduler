package pj.domain.schedule

import org.scalatest.funsuite.AnyFunSuite
import pj.io.FileIO.load

import scala.xml.*

class ScheduleMS03GraphTest extends AnyFunSuite:

  test("Valid agenda 01"):
      val r = for
        xml <- load("files/assessment/ms03/valid_agenda_01_in.xml")
        result <- ScheduleMS03.create(xml)
//        expected <- load("files/test/ms03/valid_agenda_01_out.xml")
      yield
        val prettyPrinter = new PrettyPrinter(80, 2) // 80 is the width of the line, 2 is the number of spaces for indentation
        val prettyXml = prettyPrinter.formatNodes(result)

        //println(Utility.trim(expected))

//        val prettyPrinter2 = new PrettyPrinter(80, 2) // 80 is the width of the line, 2 is the number of spaces for indentation
//        val prettyXml2 = prettyPrinter2.formatNodes(expected)
//        println(prettyXml2)


//        assert(Utility.trim(result) == Utility.trim(expected))
      assert(r.isRight)
