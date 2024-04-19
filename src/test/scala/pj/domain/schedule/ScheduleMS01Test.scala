package pj.domain.schedule

import org.scalatest.funsuite.AnyFunSuite
import pj.io.FileIO.load

import scala.xml.*

class ScheduleMS01Test extends AnyFunSuite, ScheduleMS01Behaviours:

  val PATH = "files/test/ms01" // Schedule file path
  performTests(ScheduleMS01.create, "Milestone 1")

  test("Simple 01"):
    for
      xml <- load("files/test/ms01/simple01.xml")
      result <- ScheduleMS01.create(xml)
      expected <- load("files/test/ms01/simple01_out.xml")
    yield
      // Create a PrettyPrinter
      val printer = new PrettyPrinter(120, 4)
      // Use the PrettyPrinter to format the XML
      val resultXml: String = printer.format(result)
      val expectedXml: String = printer.format(expected)
      assert(resultXml === expectedXml)
      assert(Utility.trim(result) == Utility.trim(expected))

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
        assert(resultXml === expectedXml)
        assert(Utility.trim(result) == Utility.trim(expected))

  test("Invalid agenda 01"):
    for
      xml <- load("files/assessment/ms01/invalid_agenda_01_in.xml")
      result <- ScheduleMS01.create(xml)
      expected <- load("files/assessment/ms01/invalid_agenda_01_outError.xml")
    yield
      // Create a PrettyPrinter
      val printer = new PrettyPrinter(120, 4)
      // Use the PrettyPrinter to format the XML
      val resultXml: String = printer.format(result)
      val expectedXml: String = printer.format(expected)
      assert(resultXml === expectedXml)
      assert(Utility.trim(result) == Utility.trim(expected))

  test("Simple 02"):
    for
      xml <- load("files/test/ms01/simple02_in.xml")
      result <- ScheduleMS01.create(xml)
      expected <- load("files/test/ms01/simple02_out.xml")
    yield
      // Create a PrettyPrinter
      val printer = new PrettyPrinter(120, 4)
      // Use the PrettyPrinter to format the XML
      val resultXml: String = printer.format(result)
      val expectedXml: String = printer.format(expected)
      assert(resultXml === expectedXml)
      assert(Utility.trim(result) == Utility.trim(expected))

  test("Simple 03"):
    for
      xml <- load("files/test/ms01/simple03_in.xml")
      result <- ScheduleMS01.create(xml)
      expected <- load("files/test/ms01/simple03_outError.xml")
    yield
      // Create a PrettyPrinter
      val printer = new PrettyPrinter(120, 4)
      // Use the PrettyPrinter to format the XML
      val resultXml: String = printer.format(result)
      val expectedXml: String = printer.format(expected)
      assert(resultXml === expectedXml)
      assert(Utility.trim(result) == Utility.trim(expected))