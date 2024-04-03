package pj.domain

import pj.domain.schedule.Role

type Result[A] = Either[DomainError,A]

enum DomainError:
  case IOFileProblem(error: String)
  case XMLError(error: String)
  case InvalidDuration(value: String)
  case InvalidTitle(value: String)
  case InvalidName(value: String)
  case InvalidStudent(value: String)
  case InvalidTeacherId(value: String)
  case InvalidExternalId(value: String)
  case InvalidJury(value: Set[Role])
  case InvalidDateTime(value: String)
  case InvalidPreference(value: Int)
  
