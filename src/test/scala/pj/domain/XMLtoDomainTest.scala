package pj.domain

import org.scalatest.funsuite.AnyFunSuite

import scala.language.adhocExtensions

class XMLtoDomainTest extends AnyFunSuite:

  test("valid agenda xml should return agenda"):
    val agendaXml =
      <agenda xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="../../agenda.xsd"
              duration="01:00:00">
        <vivas>
          <viva student="Student 001" title="Title 1">
            <president id="T001"/>
            <advisor id="T002"/>
            <supervisor id="E001"/>
          </viva>
          <viva student="Student 002" title="Title 2">
            <president id="T002"/>
            <advisor id="T001"/>
            <supervisor id="E001"/>
          </viva>
        </vivas>
        <resources>
          <teachers>
            <teacher id="T001" name="Teacher 001">
              <availability start="2024-05-30T09:30:00" end="2024-05-30T12:30:00" preference="5"/>
              <availability start="2024-05-30T13:30:00" end="2024-05-30T16:30:00" preference="3"/>
            </teacher>
            <teacher id="T002" name="Teacher 002">
              <availability start="2024-05-30T10:30:00" end="2024-05-30T11:30:00" preference="5"/>
              <availability start="2024-05-30T14:30:00" end="2024-05-30T17:00:00" preference="5"/>
            </teacher>
          </teachers>
          <externals>
            <external id="E001" name="External 001">
              <availability start="2024-05-30T10:00:00" end="2024-05-30T13:30:00" preference="2"/>
              <availability start="2024-05-30T15:30:00" end="2024-05-30T18:00:00" preference="5"/>
            </external>
            <external id="E002" name="External 002">
              <availability start="2024-05-30T10:00:00" end="2024-05-30T13:30:00" preference="1"/>
            </external>
          </externals>
        </resources>
      </agenda>
//      val expected = Left(InvalidAgenda())
//      val result = agenda(agendaXml)
//      assert(result === expected)

//  test("ensure valid teacher is valid"):
//    val id = "T002"
//    val name = "Teacher 002"
//    val start = "2024-05-30T10:30:00"
//    val end = "2024-05-30T11:30:00"
//    val preference = "5"
//    val teacherXml =
//      <teacher id={id} name={name}>
//        <availability start={start} end={end} preference={preference}/>
//      </teacher>
//
//    for
//      tid  <-  TeacherId.from(id)
//      name <-
//    yield
//      val expected = Teacher()
//      val result = Right(teacher(teacherXml))
//      assert(result === expected)
