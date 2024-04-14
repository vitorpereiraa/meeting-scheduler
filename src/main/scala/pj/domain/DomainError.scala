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
  case AvailabilityNotFound(student: Student, startTime: DateTime, endTime: DateTime)
  case ResourceInvalid(value: ResourceId)
  
