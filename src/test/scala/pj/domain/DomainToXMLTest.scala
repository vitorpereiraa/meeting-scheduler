package pj.domain

import org.scalatest.funsuite.AnyFunSuite
import pj.domain.DomainError.*
import pj.domain.Role.*
import pj.domain.SimpleTypes.*
import pj.xml.DomainToXML

import scala.language.adhocExtensions
import scala.xml.Utility.trim
import scala.xml.{Elem, PrettyPrinter}

class DomainToXMLTest extends AnyFunSuite:
  test("DomainToXML"):  
    for
      date1         <- DateTime.from("2022-01-01T09:00:00")
      date2         <- DateTime.from("2022-01-01T10:00:00")
      date3         <- DateTime.from("2022-01-02T09:00:00")
      date4         <- DateTime.from("2022-01-02T10:00:00")
      date5         <- DateTime.from("2022-01-03T09:00:00")
      date6         <- DateTime.from("2022-01-03T10:00:00")
      pref1         <- Preference.from(1)
      pref2         <- Preference.from(2)
      pref3         <- Preference.from(3)
      availability1 = Availability(date1, date2, pref1)
      availability2 = Availability(date3, date4, pref2)
      availability3 = Availability(date5, date6, pref3)
      tid1          <- TeacherId.from("T001")
      nameT1        <- Name.from("Teacher 1")
      tid2          <- TeacherId.from("T002")
      nameT2        <- Name.from("Teacher 2")
      externalId    <- ExternalId.from("E001")
      externalName  <- Name.from("External 1")
      teacher1 = Teacher(tid1, nameT1, List(availability1, availability2))
      teacher2 = Teacher(tid2, nameT2, List(availability2, availability3))
      external1 = External(externalId, externalName, List(availability1, availability3))
      jury = List(
        Role.President(teacher1),
        Role.Advisor(teacher2),
        Role.Supervisor(external1)
      )
      student       <- Student.from("John")
      title         <- Title.from("Title")
      start         <- DateTime.from("2022-01-01T09:00:00")
      end           <- DateTime.from("2022-01-01T10:00:00")
      preference    <- SummedPreference.from(4)
      totalPref     <- SummedPreference.from(4)

      vivaSchedule = ScheduledViva(student, title, jury, start, end, preference)
      outputSchedule = CompleteSchedule(List(vivaSchedule), totalPref)
    yield
      val xml = DomainToXML.generateOutputXML(outputSchedule)
      // Create a PrettyPrinter
      val printer = new PrettyPrinter(120, 4)
      // Use the PrettyPrinter to format the XML
      val formattedXml: String = printer.format(xml)
      def expected: Elem =
        <schedule xsi:noNamespaceSchemaLocation="../../schedule.xsd" totalPreference="4"
                  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
          <viva student="John" title="Title" start="2022-01-01T09:00:00" end="2022-01-01T10:00:00" preference="4">
            <president name="Teacher 1"/>
            <advisor name="Teacher 2"/>
            <supervisor name="External 1"/>
          </viva>
        </schedule>
      val expectedXml: String = printer.format(expected)
      assert(formattedXml != null)
      assert(formattedXml === expectedXml)
