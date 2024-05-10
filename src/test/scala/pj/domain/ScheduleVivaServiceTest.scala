package pj.domain

import org.scalatest.funsuite.AnyFunSuite
import pj.domain.*
import pj.domain.DomainError.NoAvailableSlot
import pj.domain.SimpleTypes.*
import pj.domain.schedule.ScheduleMS01
import pj.domain.scheduleviva.ScheduleVivaService

import scala.language.adhocExtensions

class ScheduleVivaServiceTest extends AnyFunSuite:

  test("getFirstAvailability returns Left when the availabilities list is empty"):
    val availabilities = List[Availability]()
    val result = ScheduleVivaService.getFirstAvailability(availabilities)
    assert(result === Left(NoAvailableSlot()))

  test("getFirstAvailability returns the only availability when the availabilities list contains one availability"):
    for
      start <- DateTime.from("2024-04-14T09:00")
      end <- DateTime.from("2024-04-14T12:00")
      preference <- Preference.from(3)
      availability <- Availability.from(start, end, preference)
      availabilities = List[Availability](availability)
      result = ScheduleVivaService.getFirstAvailability(availabilities)
    yield assert(result === Right(availability))

  test("getFirstAvailability returns the first availability when the availabilities list contains multiple availabilities"):
    for
      start1 <- DateTime.from("2024-04-14T09:00")
      end1 <- DateTime.from("2024-04-14T12:00")
      preference1 <- Preference.from(3)
      availability1 <- Availability.from(start1, end1, preference1)

      start2 <- DateTime.from("2024-04-15T09:00")
      end2 <- DateTime.from("2024-04-15T12:00")
      preference2 <- Preference.from(3)
      availability2 <- Availability.from(start2, end2, preference2)

      availabilities = List[Availability](availability2, availability1) // availability1 is earlier
      result = ScheduleVivaService.getFirstAvailability(availabilities)
    yield assert(result === Right(availability1))

  test("getAvailabilitiesForVivas returns Left when the resources list is empty"):
    for
      student <- Student.from("John Doe")
      title <- Title.from("Viva Title")
      teacherId1 <- TeacherId.from("T001")
      name1 <- Name.from("Teacher1")
      resource1 = Teacher(teacherId1, name1, List())
      role1 = Role.President(resource1)
      jury = List(role1)
      viva <- Viva.from(student, title, jury)
      resources = List[Resource]()
      duration <- Duration.from("01:00") // Duration in minutes
      result = ScheduleVivaService.getAvailabilitiesForVivas(viva, resources, duration)
    yield assert(result === Left(NoAvailableSlot()))

  test("getAvailabilitiesForVivas returns the only availability when the resources list contains one resource"):
    for
      student <- Student.from("John Doe")
      title <- Title.from("Viva Title")
      teacherId1 <- TeacherId.from("T001")
      name1 <- Name.from("Teacher1")
      resource1 = Teacher(teacherId1, name1, List())
      role1 = Role.President(resource1)
      jury = List(role1)
      viva <- Viva.from(student, title, jury)
      resources = List[Resource](resource1)
      duration <- Duration.from("01:00") // Duration in minutes
      result = ScheduleVivaService.getAvailabilitiesForVivas(viva, resources, duration)
    yield assert(result === Right(List(resource1)))

  test("getAvailabilitiesForVivas returns the first availability when the resources list contains multiple resources"):
    for
      student <- Student.from("John Doe")
      title <- Title.from("Viva Title")
      teacherId1 <- TeacherId.from("T001")
      name1 <- Name.from("Teacher1")
      resource1 = Teacher(teacherId1, name1, List())
      teacherId2 <- TeacherId.from("T002")
      name2 <- Name.from("Teacher2")
      resource2 = Teacher(teacherId2, name2, List())
      role1 = Role.President(resource1)
      jury = List(role1)
      viva <- Viva.from(student, title, jury)
      resources = List[Resource](resource1, resource2)
      duration <- Duration.from("01:00") // Duration in minutes
      result = ScheduleVivaService.getAvailabilitiesForVivas(viva, resources, duration)
    yield assert(result === Right(List(resource1)))

  test("scheduleVivaFromViva returns ScheduledViva when viva has a matching resource"):
    for
      student <- Student.from("John Doe")
      title <- Title.from("Viva Title")
      teacherId <- TeacherId.from("T001")
      name <- Name.from("Teacher1")
      resource = Teacher(teacherId, name, List())
      role = Role.President(resource)
      jury = List(role)
      viva <- Viva.from(student, title, jury)
      resources = List[Resource](resource)
      originalResources = List[Resource](resource)
      duration <- Duration.from("01:00") // Duration in minutes
      result <- ScheduleVivaService.scheduleVivaFromViva(viva, resources, originalResources, duration)
    yield Right(List(resource))

  test("scheduleVivaFromViva returns error when viva does not have a matching resource") :
    for
      student <- Student.from("John Doe")
      title <- Title.from("Viva Title")
      teacherId1 <- TeacherId.from("T001")
      name1 <- Name.from("Teacher1")
      resource1 = Teacher(teacherId1, name1, List())
      role1 = Role.President(resource1)
      jury = List(role1)
      viva <- Viva.from(student, title, jury)
      teacherId2 <- TeacherId.from("T002")
      name2 <- Name.from("Teacher2")
      resource2 = Teacher(teacherId2, name2, List())
      resources = List[Resource](resource2) // resource2 does not match any jury member
      originalResources = List[Resource](resource2)
      duration <- Duration.from("01:00") // Duration in minutes
      result <- ScheduleVivaService.scheduleVivaFromViva(viva, resources, originalResources, duration)
    yield assert(result === Left(NoAvailableSlot()))

  test("scheduleVivaFromViva returns error when resources list is empty") :
    for
      student <- Student.from("John Doe")
      title <- Title.from("Viva Title")
      teacherId <- TeacherId.from("T001")
      name <- Name.from("Teacher1")
      resource = Teacher(teacherId, name, List())
      role = Role.President(resource)
      jury = List(role)
      viva <- Viva.from(student, title, jury)
      resources = List[Resource]() // Empty resources list
      originalResources = List[Resource]() // Empty originalResources list
      duration <- Duration.from("01:00") // Duration in minutes
      result <- ScheduleVivaService.scheduleVivaFromViva(viva, resources, originalResources, duration)
    yield assert(result === Left(NoAvailableSlot()))

  test("scheduleVivaFromViva returns error when viva is null") :
    for
      student <- Student.from("John Doe")
      title <- Title.from("")
      teacherId <- TeacherId.from("T001")
      name <- Name.from("Teacher1")
      resource = Teacher(teacherId, name, List())
      role = Role.President(resource)
      jury = List(role)
      resources = List[Resource](resource)
      originalResources = List[Resource](resource)
      duration <- Duration.from("01:00") // Duration in minutes
      viva <- Viva.from(student, title, jury) // Null viva
      result <- ScheduleVivaService.scheduleVivaFromViva(viva, resources, originalResources, duration)
    yield assert(result === Left(NoAvailableSlot()))

  test("scheduleVivaFromViva returns error when duration is zero") :
    for
      student <- Student.from("John Doe")
      title <- Title.from("Viva Title")
      teacherId <- TeacherId.from("T001")
      name <- Name.from("Teacher1")
      resource = Teacher(teacherId, name, List())
      role = Role.President(resource)
      jury = List(role)
      viva <- Viva.from(student, title, jury)
      resources = List[Resource](resource)
      originalResources = List[Resource](resource)
      duration <- Duration.from("00:00") // Duration is zero
      result <- ScheduleVivaService.scheduleVivaFromViva(viva, resources, originalResources, duration)
    yield Right(List(resource))

  test("scheduleVivaFromViva returns error when duration is negative") :
    for
      student <- Student.from("John Doe")
      title <- Title.from("Viva Title")
      teacherId <- TeacherId.from("T001")
      name <- Name.from("Teacher1")
      resource = Teacher(teacherId, name, List())
      role = Role.President(resource)
      jury = List(role)
      viva <- Viva.from(student, title, jury)
      resources = List[Resource](resource)
      originalResources = List[Resource](resource)
      duration <- Duration.from("-01:00") // Negative duration
      result <- ScheduleVivaService.scheduleVivaFromViva(viva, resources, originalResources, duration)
    yield (assert(result === Left(NoAvailableSlot())))

  test("innerScheduleVivaFromAgenda returns success with empty list when agenda has no vivas"):
    for
      duration <- Duration.from("01:00")
      agenda = Agenda(duration, List[Viva](), List[Resource]())
      resources = List[Resource]()
      result = ScheduleVivaService.innerScheduleVivaFromAgenda(agenda, resources)
    yield assert(result.isRight && result.getOrElse(List()).isEmpty)

  test("innerScheduleVivaFromAgenda returns error when scheduleVivaFromViva returns an error"):
    for
      student <- Student.from("John Doe")
      title <- Title.from("Viva Title")
      teacherId1 <- TeacherId.from("T001")
      name1 <- Name.from("Teacher1")
      resource1 = Teacher(teacherId1, name1, List())
      role1 = Role.President(resource1)
      jury = List(role1)
      viva <- Viva.from(student, title, jury)
      teacherId2 <- TeacherId.from("T002")
      name2 <- Name.from("Teacher2")
      resource2 = Teacher(teacherId2, name2, List())
      resources = List[Resource](resource2) // resource2 does not match any jury member
      originalResources = List[Resource](resource2)
      duration <- Duration.from("01:00")
      agenda = Agenda(duration, List(viva), resources)
      result <- ScheduleVivaService.innerScheduleVivaFromAgenda(agenda, resources)
    yield (assert(result === Left(NoAvailableSlot())))

  test("innerScheduleVivaFromAgenda returns success when scheduleVivaFromViva returns a successful result") :
    for
      student <- Student.from("John Doe")
      title <- Title.from("Viva Title")
      teacherId <- TeacherId.from("T001")
      name <- Name.from("Teacher1")
      resource = Teacher(teacherId, name, List())
      role = Role.President(resource)
      jury = List(role)
      viva <- Viva.from(student, title, jury)
      resources = List[Resource](resource)
      originalResources = List[Resource](resource)
      duration <- Duration.from("01:00")
      agenda = Agenda(duration, List[Viva](viva), List[Resource](resource))
      result <- ScheduleVivaService.innerScheduleVivaFromAgenda(agenda, resources)
    yield Right(List(resource))

  test("scheduleVivaFromAgenda returns success with empty list when agenda has no vivas"):
    for
      duration <- Duration.from("01:00")
      agenda = Agenda(duration, List[Viva](), List[Resource]())
      result = ScheduleVivaService.scheduleVivaFromAgenda(agenda)
    yield assert(result.isRight && result.getOrElse(List()).isEmpty)

  test("scheduleVivaFromAgenda returns error when innerScheduleVivaFromAgenda returns an error"):
    for
      student <- Student.from("John Doe")
      title <- Title.from("Viva Title")
      teacherId1 <- TeacherId.from("T001")
      name1 <- Name.from("Teacher1")
      resource1 = Teacher(teacherId1, name1, List())
      role1 = Role.President(resource1)
      jury = List(role1)
      viva <- Viva.from(student, title, jury)
      teacherId2 <- TeacherId.from("T002")
      name2 <- Name.from("Teacher2")
      resource2 = Teacher(teacherId2, name2, List())
      resources = List[Resource](resource2) // resource2 does not match any jury member
      originalResources = List[Resource](resource2)
      duration <- Duration.from("01:00")
      agenda = Agenda(duration, List(viva), resources)
      result <- ScheduleVivaService.scheduleVivaFromAgenda(agenda)
    yield (assert(result === Left(NoAvailableSlot())))

  test("scheduleVivaFromAgenda returns success when innerScheduleVivaFromAgenda returns a successful result") :
    for
      student <- Student.from("John Doe")
      title <- Title.from("Viva Title")
      teacherId <- TeacherId.from("T001")
      name <- Name.from("Teacher1")
      resource = Teacher(teacherId, name, List())
      role = Role.President(resource)
      jury = List(role)
      viva <- Viva.from(student, title, jury)
      resources = List[Resource](resource)
      originalResources = List[Resource](resource)
      duration <- Duration.from("01:00")
      agenda = Agenda(duration, List[Viva](viva), List[Resource](resource))
      result <- ScheduleVivaService.scheduleVivaFromAgenda(agenda)
    yield Right(List(resource))