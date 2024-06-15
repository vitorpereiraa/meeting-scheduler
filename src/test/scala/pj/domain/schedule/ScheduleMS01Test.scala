package pj.domain.schedule

import org.scalatest.funsuite.AnyFunSuite
import pj.io.FileIO.load

import scala.xml.*

class ScheduleMS01Test extends AnyFunSuite, ScheduleBehaviours:

  val PATH = "files/test/ms01" // Schedule file path
  performTests(ScheduleMS01.create, "Milestone 1")

  test("Simple 01"):
    for
      xml <- load("files/test/ms01/simple01_in.xml")
      result <- ScheduleMS01.create(xml)
      expected <- load("files/test/ms01/simple01_out.xml")
    yield assert(Utility.trim(result) == Utility.trim(expected))

  test("Valid agenda 01"):
      for
        xml <- load("files/assessment/ms01/group_valid_agenda_01_in.xml")
        result <- ScheduleMS01.create(xml)
        expected <- load("files/assessment/ms01/group_valid_agenda_01_out.xml")
      yield assert(Utility.trim(result) == Utility.trim(expected))

  test("Invalid agenda 01"):
    for
      xml <- load("files/assessment/ms01/invalid_agenda_01_in.xml")
      result <- ScheduleMS01.create(xml)
      expected <- load("files/assessment/ms01/invalid_agenda_01_outError.xml")
    yield assert(Utility.trim(result) == Utility.trim(expected))

  test("Simple 02"):
    for
      xml <- load("files/test/ms01/simple02_in.xml")
      result <- ScheduleMS01.create(xml)
      expected <- load("files/test/ms01/simple02_out.xml")
    yield assert(Utility.trim(result) == Utility.trim(expected))

  test("Simple 03"):
    for
      xml <- load("files/test/ms01/simple03_in.xml")
      result <- ScheduleMS01.create(xml)
      expected <- load("files/test/ms01/simple03_outError.xml")
    yield assert(Utility.trim(result) == Utility.trim(expected))

  test("Simple 04"):
    for
      xml <- load("files/test/ms01/simple04_in.xml")
      result <- ScheduleMS01.create(xml)
      expected <- load("files/test/ms01/simple04_outError.xml")
    yield assert(Utility.trim(result) == Utility.trim(expected))

  test("Simple 05"):
    for
      xml <- load("files/test/ms01/simple05_in.xml")
      result <- ScheduleMS01.create(xml)
      expected <- load("files/test/ms01/simple05_out.xml")
    yield assert(Utility.trim(result) == Utility.trim(expected))