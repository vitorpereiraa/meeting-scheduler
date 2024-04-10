import pj.domain.Result
import pj.domain.schedule.{Agenda, President, Role, Viva}
import pj.domain.schedule.SimpleTypes.{Duration, Student, TeacherId, Title}
import pj.xml.XML.*

import scala.xml.{Elem, Node, NodeSeq, UnprefixedAttribute}

val xml : Elem =
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

def duration(xml: Node): Result[Duration] =
    for
        durationStr <- fromAttribute(xml, "duration")
        duration    <- Duration.from(durationStr)
    yield duration

duration(xml)

def roles(xml: Node): Result[Set[Role]] =
    for
        presidentNode  <- fromNode(xml, "president")
        presidentIdStr <- fromAttribute(presidentNode, "id")
        presidentId    <- TeacherId.from(presidentIdStr)
        president      <- Right(President(presidentId))
    yield Set(president)

def viva(xml: Node): Result[Viva] =
    for
      studentStr <- fromAttribute(xml, "student")
      student    <- Student.from(studentStr)
      titleStr   <- fromAttribute(xml, "title")
      title      <- Title.from(titleStr)
      roles      <- roles(xml)
      viva       <- Viva.from(student, title, roles)
    yield viva

def vivas(xml: NodeSeq): Result[List[Viva]] =
    traverse(xml \ "viva", viva)

vivas(xml \ "vivas")

(xml \\ "viva").map(v => fromNode(v, "president"))