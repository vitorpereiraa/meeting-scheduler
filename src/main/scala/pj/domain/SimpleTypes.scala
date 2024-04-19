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

    def from(hour: Int, minute: Int): Result[Duration] =
      Try(LocalTime.of(hour, minute))
        .fold(
          error => Left(InvalidDuration(hour.toString + minute.toString)),
          success => Right(success)
        )

    def between(start: DateTime, end: DateTime): Result[Duration] =
      val startMinutes = start.getHour * 60 + start.getMinute
      val endMinutes = end.getHour * 60 + end.getMinute
      val durationMinutes = endMinutes - startMinutes
      from(durationMinutes / 60, durationMinutes % 60)

  extension (d: Duration)
    @targetName("DurationTo")
    def to: String = d.format(DateTimeFormatter.ISO_LOCAL_TIME)
    def toLocalTime: LocalTime = d
    def toMinutes: Int = d.getHour * 60 + d.getMinute
    def isBefore(other: Duration): Boolean = d.isBefore(other)

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
    def compareTo(other: Student): Int = s.compareTo(other)

  opaque type DateTime = LocalDateTime
  object DateTime:
    implicit val dateTimeOrdering: Ordering[DateTime] = Ordering.fromLessThan(_.isBefore(_))
    
    def from(dateTimeString: String): Result[DateTime] =
      Try(LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_LOCAL_DATE_TIME))
        .fold(
          error => Left(InvalidDateTime(dateTimeString)),
          success => Right(success)
        )
    def isEndTimeAfterStartTime (start: DateTime, end: DateTime): Result[Boolean] =
      if(start.compareTo(end)) < 0 then Right(true) else Left(InvalidDateTime(end.toString))

    def isBetween (start: DateTime, end: DateTime, dateTime: DateTime): Boolean =
      if(dateTime.compareTo(start) >= 0 && dateTime.compareTo(end) <= 0) then true else false
    
    def max(date1: DateTime, date2: DateTime): DateTime = if (date1.compareTo(date2) > 0) then date1 else date2

  extension (d: DateTime)
    @targetName("DateTimeTo")
    def to: String = d.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    def toLocalDateTime: LocalDateTime = d
    def isAfter(other: DateTime): Boolean = d.isAfter(other)
    def isBefore(other: DateTime): Boolean = d.isBefore(other) 
    def isEqual(other: DateTime): Boolean = d.isEqual(other)
    def max(other: DateTime): DateTime = if d.isAfter(other) then d else other
    def min(other: DateTime): DateTime = if d.isBefore(other) then d else other
    def minus(other: Duration): DateTime =
      val totalMinutes = other.toLocalTime.getHour * 60 + other.toLocalTime.getMinute
      d.minusMinutes(totalMinutes)
    def minus(other: DateTime): Duration =
      val totalMinutesD = d.getHour * 60 + d.getMinute
      val totalMinutesOther = other.toLocalTime.getHour * 60 + other.toLocalTime.getMinute
      val resultMinutes = totalMinutesD - totalMinutesOther
      LocalTime.of(resultMinutes / 60, resultMinutes % 60)
    def plus(other: Duration): DateTime =
      val totalMinutes = other.toLocalTime.getHour * 60 + other.toLocalTime.getMinute
      d.plusMinutes(totalMinutes)
    
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
