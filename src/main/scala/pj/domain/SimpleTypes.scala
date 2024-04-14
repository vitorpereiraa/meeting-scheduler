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
    def toLocalTime: LocalTime = d

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
    def isEndTimeAfterStartTime (start: DateTime, end: DateTime): Result[Boolean] =
      if(start.compareTo(end)) < 0 then Right(true) else Left(InvalidDateTime(end.toString))

    def isBetween (start: DateTime, end: DateTime, dateTime: DateTime): Boolean =
      if(dateTime.compareTo(start) >= 0 && dateTime.compareTo(end) <= 0) then true else false

  extension (d: DateTime)
    @targetName("DateTimeTo")
    def to: String = d.toString
    def isAfter(other: DateTime): Boolean = d.isAfter(other)
    def isBefore(other: DateTime): Boolean = d.isBefore(other)
    def minus(other: Duration): DateTime = d.minusHours(other.toLocalTime.getHour).minusMinutes(other.toLocalTime.getMinute)
    def plus(other: Duration): DateTime = d.plusHours(other.toLocalTime.getHour).plusMinutes(other.toLocalTime.getMinute)

        
  opaque type Preference = Int
  object Preference:
    private val upperLimit: Int = 5
    private val lowerLimit: Int = 1
    def from(value: Int): Result[Preference] =
      if value <= upperLimit && value >= lowerLimit then
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
    private val lowerLimit: Int = 1
    def from(value: Int): Result[SummedPreference] =
      if value >= lowerLimit then
        Right(value) else
        Left(InvalidPreference(value.toString))

  extension (sp: SummedPreference)
    @targetName("SummedPreferenceTo")
    def to: Int = sp
