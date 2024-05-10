package pj.domain

import org.scalatest.funsuite.AnyFunSuite
import pj.domain.SimpleTypes.*

class DomainTest extends AnyFunSuite:

  test("Teacher should be a Resource"):
    for
      teacherId <- TeacherId.from("T001")
      name <- Name.from("John Doe")
      start <- DateTime.from("2022-01-01T09:00:00")
      end <- DateTime.from("2022-01-01T10:00:00")
      preference <- Preference.from(1)
      availability <- Availability.from(start, end, preference)
      teacher = Teacher(teacherId, name, List(availability))
      isResource = teacher.getClass == classOf[Resource]
    yield  assert(!isResource, "Teacher is not a Resource")


  test("Availability should have valid start, end and preference"):
    for
      start <- DateTime.from("2022-01-01T09:00:00")
      end <- DateTime.from("2022-01-01T10:00:00")
      preference <- Preference.from(1)
      availability <- Availability.from(start, end, preference)
    yield
      assert(availability.start == start)
      assert(availability.end == end)
      assert(availability.preference == preference)

  test("ScheduledViva should have valid student, title, jury, start, end and preference"):
    for
      student <- Student.from("Student 001")
      title <- Title.from("Title")
      teacherId <- TeacherId.from("T001")
      name <- Name.from("John Doe")
      start <- DateTime.from("2022-01-01T09:00:00")
      end <- DateTime.from("2022-01-01T10:00:00")
      preference <- SummedPreference.from(1)
      teacher = Teacher(teacherId, name, List())
      role = Role.President(teacher)
      scheduledViva = ScheduledViva(student, title, List(role), start, end, preference)
    yield
      assert(scheduledViva.student == student)
      assert(scheduledViva.title == title)
      assert(scheduledViva.jury.contains(role))
      assert(scheduledViva.start == start)
      assert(scheduledViva.end == end)
      assert(scheduledViva.preference == preference)

  test("CompleteSchedule should have valid scheduledVivaList and totalPreference"):
    for
      student <- Student.from("Student 001")
      title <- Title.from("Title")
      teacherId <- TeacherId.from("T001")
      name <- Name.from("John Doe")
      start <- DateTime.from("2022-01-01T09:00:00")
      end <- DateTime.from("2022-01-01T10:00:00")
      preference <- SummedPreference.from(1)
      teacher = Teacher(teacherId, name, List())
      role = Role.President(teacher)
      scheduledViva = ScheduledViva(student, title, List(role), start, end, preference)
      completeSchedule = CompleteSchedule(List(scheduledViva), preference)
    yield
      assert(completeSchedule.scheduledVivaList.contains(scheduledViva))
      assert(completeSchedule.totalPreference == preference)
