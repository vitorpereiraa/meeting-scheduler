package pj.domain

import org.scalatest.funsuite.AnyFunSuite
import pj.domain.SimpleTypes.*
import pj.xml.XMLtoDomain
import pj.domain.DomainError.*

import scala.collection.immutable.Nil.:::
import scala.language.adhocExtensions

private class XMLtoDomainTest extends AnyFunSuite:

  test("ensure valid availability is valid"):
    val start = "2024-05-30T09:30:00"
    val end = "2024-05-30T12:30:00"
    val preference = "5"
    val availabilityXml = <availability start={start} end={end} preference={preference}/>
    for
      s <- DateTime.from(start)
      e <- DateTime.from(end)
      p <- Preference.from(preference)
    yield
      val expected = Right(Availability(s, e, p))
      val result = XMLtoDomain.availability(availabilityXml)
      assert(result === expected)

  test("ensure valid teacher is valid"):
    val id = "T002"
    val name = "Teacher 002"
    val start = "2024-05-30T10:30:00"
    val end = "2024-05-30T11:30:00"
    val preference = "5"
    val teacherXml =
      <teacher id={id} name={name}>
        <availability start={start} end={end} preference={preference}/>
      </teacher>
    for
      tid   <- TeacherId.from(id)
      tname <- Name.from(name)
      s     <- DateTime.from(start)
      e     <- DateTime.from(end)
      p     <- Preference.from(preference)
      av    = Availability(s, e, p)
    yield
      val expected = Right(Teacher(tid, tname, List(av)))
      val result = XMLtoDomain.teacher(teacherXml)
      assert(result === expected)

  test("ensure valid external is valid"):
    val id = "E001"
    val name = "External 001"
    val start = "2024-05-30T10:30:00"
    val end = "2024-05-30T11:30:00"
    val preference = "5"
    val externalXml =
      <external id={id} name={name}>
        <availability start={start} end={end} preference={preference}/>
      </external>
    for
      eid   <- ExternalId.from(id)
      ename <- Name.from(name)
      s     <- DateTime.from(start)
      e     <- DateTime.from(end)
      p     <- Preference.from(preference)
      av    = Availability(s, e, p)
    yield
      val expected = Right(External(eid, ename, List(av)))
      val result = XMLtoDomain.external(externalXml)
      assert(result === expected)

  test("ensure valid resources is valid"):
    val tid = "T002"
    val tname = "Teacher 002"
    val tstart = "2024-05-30T10:30:00"
    val tend = "2024-05-30T11:30:00"
    val tpreference = "5"
    val eid = "E001"
    val ename = "External 001"
    val estart = "2024-05-30T10:30:00"
    val eend = "2024-05-30T11:30:00"
    val epreference = "5"
    val xml =
      <resources>
        <teachers>
          <teacher id={tid} name={tname}>
            <availability start={tstart} end={tend} preference={tpreference}/>
          </teacher>
        </teachers>
        <externals>
          <external id={eid} name={ename}>
            <availability start={estart} end={eend} preference={epreference}/>
          </external>
        </externals>
      </resources>
    for
      eeid <- ExternalId.from(eid)
      eename <- Name.from(ename)
      ees <- DateTime.from(estart)
      eee <- DateTime.from(eend)
      eep <- Preference.from(epreference)
      eeav = Availability(ees, eee, eep)
      externals = List(External(eeid, eename, List(eeav)))
      ttid   <- TeacherId.from(tid)
      ttname <- Name.from(tname)
      tts     <- DateTime.from(tstart)
      tte     <- DateTime.from(tend)
      ttp     <- Preference.from(tpreference)
      ttav    = Availability(tts, tte, ttp)
      teachers = List(Teacher(ttid, ttname, List(ttav)))
    yield
      val expected = Right(teachers ::: externals)
      val result = XMLtoDomain.resources(xml)
      assert(result === expected)

  test("ensure valid viva is valid"):
    val tid2 = "T002"
    val tname2 = "Teacher 002"
    val tid = "T001"
    val tname = "Teacher 001"
    val tstart = "2024-05-30T10:30:00"
    val tend = "2024-05-30T11:30:00"
    val tpreference = "5"
    val eid = "E001"
    val ename = "External 001"
    val estart = "2024-05-30T10:30:00"
    val eend = "2024-05-30T11:30:00"
    val epreference = "5"
    val student1 = "Student 001"
    val title1 = "Title 1"
    val xml =
      <viva student={student1} title={title1}>
        <president id={tid}/>
        <advisor id={tid2}/>
        <supervisor id={eid}/>
      </viva>
    for
      eeid <- ExternalId.from(eid)
      eename <- Name.from(ename)
      ees <- DateTime.from(estart)
      eee <- DateTime.from(eend)
      eep <- Preference.from(epreference)
      eeav = Availability(ees, eee, eep)
      external = External(eeid, eename, List(eeav))
      externals = List(external)
      ttid <- TeacherId.from(tid)
      ttname <- Name.from(tname)
      ttid2 <- TeacherId.from(tid2)
      ttname2 <- Name.from(tname2)
      tts <- DateTime.from(tstart)
      tte <- DateTime.from(tend)
      ttp <- Preference.from(tpreference)
      ttav = Availability(tts, tte, ttp)
      teacher = Teacher(ttid, ttname, List(ttav))
      teacher2 = Teacher(ttid2, ttname2, List(ttav))
      teachers = List(teacher, teacher2)
      resources = teachers ::: externals
      student <- Student.from(student1)
      title <- Title.from(title1)
      president = Role.President(teacher)
      advisor = Role.Advisor(teacher2)
      supervisor = Role.Supervisor(external)
      viva = Viva.from(student, title, List(president, advisor, supervisor))
    yield
      val expected = viva
      val result = XMLtoDomain.viva(resources)(xml)
      assert(result === expected)

  test("valid agenda should return agenda"):
    val tid2 = "T002"
    val tname2 = "Teacher 002"
    val tid = "T001"
    val tname = "Teacher 001"
    val tstart = "2024-05-30T10:30:00"
    val tend = "2024-05-30T11:30:00"
    val tpreference = "5"
    val eid = "E001"
    val ename = "External 001"
    val estart = "2024-05-30T10:30:00"
    val eend = "2024-05-30T11:30:00"
    val epreference = "5"
    val student1 = "Student 001"
    val title1 = "Title 1"
    val duration = "01:00:00"
    val xml =
      <agenda duration={duration}>
        <vivas>
          <viva student={student1} title={title1}>
            <president id={tid}/>
            <advisor id={tid2}/>
            <supervisor id={eid}/>
          </viva>
        </vivas>
        <resources>
          <teachers>
            <teacher id={tid} name={tname}>
              <availability start={tstart} end={tend} preference={tpreference}/>
            </teacher>
            <teacher id={tid2} name={tname2}>
              <availability start={tstart} end={tend} preference={tpreference}/>
            </teacher>
          </teachers>
          <externals>
            <external id={eid} name={ename}>
              <availability start={estart} end={eend} preference={epreference}/>
            </external>
          </externals>
        </resources>
      </agenda>
    for
      eeid <- ExternalId.from(eid)
      eename <- Name.from(ename)
      ees <- DateTime.from(estart)
      eee <- DateTime.from(eend)
      eep <- Preference.from(epreference)
      eeav = Availability(ees, eee, eep)
      external = External(eeid, eename, List(eeav))
      externals = List(external)
      ttid <- TeacherId.from(tid)
      ttname <- Name.from(tname)
      ttid2 <- TeacherId.from(tid2)
      ttname2 <- Name.from(tname2)
      tts <- DateTime.from(tstart)
      tte <- DateTime.from(tend)
      ttp <- Preference.from(tpreference)
      ttav = Availability(tts, tte, ttp)
      teacher = Teacher(ttid, ttname, List(ttav))
      teacher2 = Teacher(ttid2, ttname2, List(ttav))
      teachers = List(teacher, teacher2)
      resources = teachers ::: externals
      student <- Student.from(student1)
      title <- Title.from(title1)
      president = Role.President(teacher)
      advisor = Role.Advisor(teacher2)
      supervisor = Role.Supervisor(external)
      viva <- Viva.from(student, title, List(president, advisor, supervisor))
      vivas = List(viva)
      duration <- Duration.from(duration)
      agenda = Agenda(duration, vivas, resources)
    yield
      val expected = Right(agenda)
      val result = XMLtoDomain.agenda(xml)
      assert(result === expected)


  test("ensure viva without president is invalid"):
    val tid2 = "T002"
    val tname2 = "Teacher 002"
    val tid = "T001"
    val tname = "Teacher 001"
    val tstart = "2024-05-30T10:30:00"
    val tend = "2024-05-30T11:30:00"
    val tpreference = "5"
    val eid = "E001"
    val ename = "External 001"
    val estart = "2024-05-30T10:30:00"
    val eend = "2024-05-30T11:30:00"
    val epreference = "5"
    val student1 = "Student 001"
    val title1 = "Title 1"
    val xml =
      <viva student={student1} title={title1}>
        <advisor id={tid2}/>
        <supervisor id={eid}/>
      </viva>
    for
      eeid <- ExternalId.from(eid)
      eename <- Name.from(ename)
      ees <- DateTime.from(estart)
      eee <- DateTime.from(eend)
      eep <- Preference.from(epreference)
      eeav = Availability(ees, eee, eep)
      external = External(eeid, eename, List(eeav))
      externals = List(external)
      ttid <- TeacherId.from(tid)
      ttname <- Name.from(tname)
      ttid2 <- TeacherId.from(tid2)
      ttname2 <- Name.from(tname2)
      tts <- DateTime.from(tstart)
      tte <- DateTime.from(tend)
      ttp <- Preference.from(tpreference)
      ttav = Availability(tts, tte, ttp)
      teacher = Teacher(ttid, ttname, List(ttav))
      teacher2 = Teacher(ttid2, ttname2, List(ttav))
      teachers = List(teacher, teacher2)
      resources = teachers ::: externals
    yield
      val expected = Left(XMLError("Node president is empty/undefined in viva"))
      val result = XMLtoDomain.viva(resources)(xml)
      assert(result === expected)

  test("ensure viva without advisor is invalid"):
    val tid2 = "T002"
    val tname2 = "Teacher 002"
    val tid = "T001"
    val tname = "Teacher 001"
    val tstart = "2024-05-30T10:30:00"
    val tend = "2024-05-30T11:30:00"
    val tpreference = "5"
    val eid = "E001"
    val ename = "External 001"
    val estart = "2024-05-30T10:30:00"
    val eend = "2024-05-30T11:30:00"
    val epreference = "5"
    val student1 = "Student 001"
    val title1 = "Title 1"
    val xml =
      <viva student={student1} title={title1}>
        <president id={tid}/>
        <supervisor id={eid}/>
      </viva>
    for
      eeid <- ExternalId.from(eid)
      eename <- Name.from(ename)
      ees <- DateTime.from(estart)
      eee <- DateTime.from(eend)
      eep <- Preference.from(epreference)
      eeav = Availability(ees, eee, eep)
      external = External(eeid, eename, List(eeav))
      externals = List(external)
      ttid <- TeacherId.from(tid)
      ttname <- Name.from(tname)
      ttid2 <- TeacherId.from(tid2)
      ttname2 <- Name.from(tname2)
      tts <- DateTime.from(tstart)
      tte <- DateTime.from(tend)
      ttp <- Preference.from(tpreference)
      ttav = Availability(tts, tte, ttp)
      teacher = Teacher(ttid, ttname, List(ttav))
      teacher2 = Teacher(ttid2, ttname2, List(ttav))
      teachers = List(teacher, teacher2)
      resources = teachers ::: externals
    yield
      val expected = Left(XMLError("Node advisor is empty/undefined in viva"))
      val result = XMLtoDomain.viva(resources)(xml)
      assert(result === expected)

  test("ensure invalid availability preference is invalid"):
    val start = "2024-05-30T09:30:00"
    val end = "2024-05-30T12:30:00"
    val preference = "6"
    val availabilityXml = <availability start={start} end={end} preference={preference}/>
    for
      s <- DateTime.from(start)
      e <- DateTime.from(end)
      p <- Preference.from(preference)
    yield
      val expected = Left(InvalidPreference(preference))
      val result = XMLtoDomain.availability(availabilityXml)
      assert(result === expected)
