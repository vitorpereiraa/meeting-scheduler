package pj.domain

import pj.domain.SimpleTypes.{DateTime, Student}

type Result[A] = Either[DomainError,A]

enum DomainError:
  case IOFileProblem(error: String)
  case XMLError(error: String)
  case InvalidDuration(value: String)
  case InvalidTitle(value: String)
  case InvalidName(value: String)
  case InvalidStudent(value: String)
  case InvalidResourceId(value: String)
  case InvalidTeacherId(value: String)
  case InvalidExternalId(value: String)
  case InvalidJury(value: List[Role])
  case InvalidDateTime(value: String)
  case InvalidEndDateTime(value: String)
  case InvalidPreference(value: String)
  case ResourceNotFound(value: ResourceId)  
  case StudentNotFound(value: String)  
  case AvailabilityNotFound(value: Availability)
  case AvailabilityNotFoundByStudent(student: Student, startTime: DateTime, endTime: DateTime)
  case ResourceInvalid(value: ResourceId)
  case NoResourcesFound()
  case NoAvailableSlot()

  override def toString: String = this match 
    case IOFileProblem(error) => s"IOFileProblem($error)"
    case XMLError(error) => s"XMLError($error)"
    case InvalidDuration(value) => value
    case InvalidTitle(value) => value
    case InvalidName(value) => value
    case InvalidStudent(value) => value
    case InvalidResourceId(value) => value
    case InvalidTeacherId(value) => value
    case InvalidExternalId(value) => value
    case InvalidJury(value) => value.mkString(", ")
    case InvalidDateTime(value) => value
    case InvalidEndDateTime(value) => value
    case InvalidPreference(value) => s"InvalidPreference($value)"
    case ResourceNotFound(value) => value.toString
    case StudentNotFound(value) => value
    case AvailabilityNotFound(value) => s"${value.start} - ${value.end}"
    case AvailabilityNotFoundByStudent(student, startTime, endTime) => s"$student - $startTime - $endTime"
    case ResourceInvalid(value) => value.toString
    case NoResourcesFound() => "No resources found"
    case NoAvailableSlot() => "ImpossibleSchedule"
  
