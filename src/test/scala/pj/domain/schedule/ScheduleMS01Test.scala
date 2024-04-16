package pj.domain.schedule

import org.scalatest.funsuite.AnyFunSuite
import pj.io.FileIO.load

import scala.xml.*

class ScheduleMS01Test extends AnyFunSuite:
  test("Valid agenda 01"):
      for
        xml <- load("files/assessment/ms01/valid_agenda_01_in.xml")
        result <- ScheduleMS01.create(xml)
        expected <- load("files/assessment/ms01/valid_agenda_01_out.xml")
      yield
        // Create a PrettyPrinter
        val printer = new PrettyPrinter(120, 4)
        // Use the PrettyPrinter to format the XML
        val resultXml: String = printer.format(result)
        val expectedXml: String = printer.format(expected)
        println("result\n" + resultXml)
        println("expected\n" + expected)
        assert(resultXml === expectedXml)
        assert(Utility.trim(result) == Utility.trim(expected))
