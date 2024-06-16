package pj.domain.schedule

import org.scalatest.funsuite.AnyFunSuite
import pj.io.FileIO.load

import scala.xml.*

class ScheduleMS03Test extends AnyFunSuite, ScheduleBehaviours:

  val PATH = "files/test/ms03" // Schedule file path
  performTests(ScheduleMS03.create, "Milestone 3")
