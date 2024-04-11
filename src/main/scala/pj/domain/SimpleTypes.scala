package pj.domain

import pj.domain.DomainError.*
import pj.domain.{DomainError, Result}

import java.time.format.DateTimeFormatter
import java.time.{LocalDateTime, LocalTime}
import scala.annotation.targetName
import scala.util.Try
import scala.util.matching.Regex

object SimpleTypes:

  opaque type Duration = LocalTime
  object Duration:
    def from(value: String): Result[Duration] =
      Try(LocalTime.parse(value))
        .fold(
          error => Left(InvalidDuration(value)),
          success => Right(success)
        )
  extension (d: Duration)
    @targetName("DurationTo")
    def to: String = d.toString

  opaque type Title = String
  object Title:
    def from(s: String): Result[Title] =
      if !s.isBlank then Right(s) else Left(InvalidTitle(s))
  extension (s: Title)
    @targetName("TitleTo")
    def to: String = s

  opaque type Name = String
  object Name:
    def from(s: String): Result[Name] =
      if !s.isBlank then Right(s) else Left(InvalidName(s))
  extension (s: Name)
    @targetName("NameTo")
    def to: String = s

  opaque type Student = String
  object Student:
    def from(s: String): Result[Student] =
      if !s.isBlank then Right(s) else Left(InvalidStudent(s))
  extension (s: Student)
    @targetName("StudentTo")
    def to: String = s

  opaque type DateTime = LocalDateTime
  object DateTime:
    private val formatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
    def from(dateTimeString: String): Result[DateTime] =
      Try(LocalDateTime.parse(dateTimeString, formatter))
        .fold(
          error => Left(InvalidDateTime(dateTimeString)),
          success => Right(success)
        )
  extension (d: DateTime)
    @targetName("DateTimeTo")
    def to: String = d.toString

  opaque type Preference = Int
  object Preference:
    val upperLimit: Int = 5
    val lowerLimit: Int = 1
    def from(value: Int): Result[Preference] =
      if value <= 5 && value >= 1 then
        Right(value) else Left(InvalidPreference(value.toString))
    def from(value: String): Result[Preference] =
      Try(value.toInt)  
        .fold(
          error   => Left(InvalidPreference(value)),
          success => from(success)
        )
  extension (p: Preference)
    @targetName("PreferenceTo")
    def to: Int = p

  opaque type SummedPreference = Int
  object SummedPreference:
    val lowerLimit: Int = 1
    def from(value: Int): Result[SummedPreference] =
      if value >= 1 then
        Right(value) else
        Left(InvalidPreference(value.toString))
  extension (sp: SummedPreference)
    @targetName("SummedPreferenceTo")
    def to: Int = sp
