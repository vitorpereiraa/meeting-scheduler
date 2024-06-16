package pj.domain.schedule

import org.scalatest.funsuite.AnyFunSuite
import pj.io.FileIO.load

import scala.xml.*

class ScheduleMS03Test extends AnyFunSuite, ScheduleBehaviours:

  val PATH = "files/test/ms03" // Schedule file path
  performTests(ScheduleMS03.create, "Milestone 3")

  test("Valid agenda 01"):
      for
        xml <- load("files/test/ms03/group_valid_agenda_01_in.xml")
        result <- ScheduleMS03.create(xml)
        expected <- load("files/test/ms03/group_valid_agenda_01_out.xml")
      yield
        //println(Utility.trim(result))
        // pretty print
        val prettyPrinter = new PrettyPrinter(80, 2) // 80 is the width of the line, 2 is the number of spaces for indentation
        val prettyXml = prettyPrinter.formatNodes(result)
//        println(prettyXml)

        //println(Utility.trim(expected))

        val prettyPrinter2 = new PrettyPrinter(80, 2) // 80 is the width of the line, 2 is the number of spaces for indentation
        val prettyXml2 = prettyPrinter2.formatNodes(expected)
//        println(prettyXml2)


        assert(Utility.trim(result) == Utility.trim(expected))

  test("Valid agenda 02"):
    for
      xml <- load("files/test/ms03/group_valid_agenda_02_in.xml")
      result <- ScheduleMS03.create(xml)
      expected <- load("files/test/ms03/group_valid_agenda_02_out.xml")
    yield
      //println(Utility.trim(result))
      // pretty print
      val prettyPrinter = new PrettyPrinter(80, 2) // 80 is the width of the line, 2 is the number of spaces for indentation
      val prettyXml = prettyPrinter.formatNodes(result)
//      println(prettyXml)

      //println(Utility.trim(expected))

      val prettyPrinter2 = new PrettyPrinter(80, 2) // 80 is the width of the line, 2 is the number of spaces for indentation
      val prettyXml2 = prettyPrinter2.formatNodes(expected)
//      println(prettyXml2)


      assert(Utility.trim(result) == Utility.trim(expected))

  test("Valid agenda 03"):
    for
      xml <- load("files/test/ms03/group_valid_agenda_03_in.xml")
      result <- ScheduleMS03.create(xml)
      expected <- load("files/test/ms03/group_valid_agenda_03_out.xml")
    yield
      //println(Utility.trim(result))
      // pretty print
      val prettyPrinter = new PrettyPrinter(80, 2) // 80 is the width of the line, 2 is the number of spaces for indentation
      val prettyXml = prettyPrinter.formatNodes(result)
//      println(prettyXml)

      //println(Utility.trim(expected))

      val prettyPrinter2 = new PrettyPrinter(80, 2) // 80 is the width of the line, 2 is the number of spaces for indentation
      val prettyXml2 = prettyPrinter2.formatNodes(expected)
//      println(prettyXml2)


      assert(Utility.trim(result) == Utility.trim(expected))