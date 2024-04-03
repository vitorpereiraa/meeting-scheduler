import pj.domain.Result
import pj.domain.schedule.Agenda
import pj.domain.schedule.SimpleTypes.Duration
import pj.xml.XML

import scala.xml.{Elem, Node, UnprefixedAttribute}

val agenda : Elem =
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

//val result = XML.traverse(agenda, f => elem)

def create(xml: Elem): Int = agenda match
    case Elem(str, str1, data, binding, node) => 1
    case _ => 0

val result = create(agenda)

//    case Elem(str, str1, data, binding, node) => 1
//    case Elem(_, "agenda", UnprefixedAttribute("duration", value, _), _*) => 1
//        val duration = Duration.from(value)
//        Agenda()

agenda \@ "duration"

(agenda \ "vivas" \ "viva")
  .map(v => v \@ "student").toList

agenda \ "resources"

agenda \\ "teacher"

(agenda \\ "teacher" \ "availability").map(a => (a \@ "start", a \@ "end")).toList

(agenda \\ "external" \ "availability")

for(viva <- agenda \ "vivas" \ "viva") print(viva)

//for {
//  duration     <- agenda \@ "duration"
//  availability <-
//} yield