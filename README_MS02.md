# Executive Report

Developed by:

- **1060503 - Pedro Fernandes**
- **1170541 - Alexandra Leite**
- **1180511 - Vitor Costa**
- **1191244 - Vitor Pereira**

## Milestone 02 Overview

Milestone 2 of the project involves creating property-based tests for the scheduling system.

The key properties tested are:
 - All the viva must be scheduled in the intervals in which its resources are available
 - One resource cannot be overlapped in two scheduled viva

To do this, we created generators for the domain entities (simple types included) and properties to test the scheduling system.

## Property Based Tests

Property-based testing is a testing methodology that involves defining properties that the system should satisfy and then generating random inputs to test these properties. This approach can help uncover edge cases and corner cases that may not be covered by traditional unit tests.

In our project we created generators for the domain entities and properties to test the scheduling system.

We have generators and property tests for the SimpleTypes, such as `DateTime` and `Duration`, and for the domain entities, such as `Viva`, `Role`, `Resource`, and `Availability`.

Here's a brief explanation of each property test:

1. **DateTime Properties**: These properties test the behavior of the `DateTime` class, such as checking that the addition and subtraction of durations is consistent and that the ordering of `DateTime` instances is correct.
2. **Duration Properties**: These properties test the behavior of the `Duration` class, such as checking that the addition and subtraction of durations is consistent and that the ordering of `Duration` instances is correct.
3. **Viva Properties**: These properties test the behavior of the `Viva` class, such as checking that the creation and manipulation of viva instances is consistent and that the properties of viva instances are correctly constructed.
4. **Role Properties**: These properties test the behavior of the `Role` class, such as checking that the creation and manipulation of role instances is consistent and that the properties of role instances are correctly constructed.
5. **Resource Properties**: These properties test the behavior of the `Resource` class, such as checking that the creation and manipulation of resource instances is consistent and that the properties of resource instances are correctly constructed.
6. **Availability Properties**: These properties test the behavior of the `Availability` class, such as checking that the creation and manipulation of availability instances is consistent and that the properties of availability instances are correctly constructed.

Then we have in ScheduleVivaServiceProperties the property tests for the scheduling system.
In this class we have the following:

The class contains several generator functions (`schedulableTeacherGen, schedulableExternalGen, schedulableResourcesGen, schedulableAvailabilityGen, schedulableAvailabilitiesGen, updateResourceAvailability, schedulableVivaGen, mergeResources, schedulableVivasGen, schedulableAgendaGen`) that generate random instances of various domain entities. 
These generators are used to provide random inputs for the property tests.  

The `property("Schedule agenda")` function is a property test that validates the scheduleVivaFromAgenda function in the ScheduleVivaService class. It uses the schedulableAgendaGen generator to generate random Agenda instances and checks that the scheduleVivaFromAgenda function can successfully schedule all vivas in the agenda.

In summary, the ScheduleVivaServiceProperties class provides a set of property-based tests for the ScheduleVivaService class. It helps ensure that the scheduling system can correctly schedule viva sessions under various conditions and constraints.

## Bugs found

The calculation of the duration between 2 date times produced wrong output when the days of the date times are different.
```scala
def fromBetween(start: DateTime, end: DateTime): Result[Duration] =
    val startMinutes = start.getHour * 60 + start.getMinute
    val endMinutes = end.getHour * 60 + end.getMinute
    val durationMinutes = endMinutes - startMinutes
    from(durationMinutes / 60, durationMinutes % 60)
```
The faulty function above was replaced with the following:

```scala
def fromBetween(start: DateTime, end: DateTime): Result[Duration] =
    Try(java.time.Duration.between(start, end))
      .fold(
        error => Left(InvalidDuration(start.toString + "between" + end.toString)),
        duration =>
          Try(LocalTime.of(duration.toHours.toInt,(duration.toMinutes % 60).toInt))
            .fold(
              error => Left(InvalidDuration(duration.toString)),
              success => Right(success)
            )
      )
```

## The First-Come First Served (FCFS) Problem

The property based test for the scheduling algorithm fails because it finds an example where the scheduling fails because of the FCFS nature of the algorithm.

For instance, assume an example agenda with a 09:40 duration requirement and with the following vivas: 

**Viva 1: Student999**

Participants and their availabilities:

1. Supervisor (ExternalId E001):
    * 9999-01-17T00:24:38 to 9999-01-17T10:04:38
    * 9999-12-01T01:44:41 to 9999-12-01T11:24:41
2. President (TeacherId T000):
    * 9999-01-17T00:24:38 to 9999-01-17T10:04:38
    * 9999-12-01T01:44:41 to 9999-12-01T11:24:41
3. Advisor (TeacherId T001):
    * 9999-01-17T00:24:38 to 9999-01-17T10:04:38
    * 9999-12-01T01:44:41 to 9999-12-01T11:24:41

**Viva 2: Student254**

Participants and their availabilities:

1. Supervisor (ExternalId E001):
    * 9999-01-17T00:24:38 to 9999-01-17T10:04:38
    * 9999-12-01T01:44:41 to 9999-12-01T11:24:41
2. CoAdvisor (ExternalId E000):
    * 9999-01-17T00:24:38 to 9999-01-17T10:04:38
3. President (TeacherId T000):
    * 9999-01-17T00:24:38 to 9999-01-17T10:04:38
    * 9999-12-01T01:44:41 to 9999-12-01T11:24:41
4. Advisor (TeacherId T001):
    * 9999-01-17T00:24:38 to 9999-01-17T10:04:38
    * 9999-12-01T01:44:41 to 9999-12-01T11:24:41

**Availability intersections:**

**For Viva 1:**

* **Participants:** Supervisor (E001), President (T000), Advisor (T001)
* **FCFS Common Time Slot chosen:** 9999-01-17T00:24:38 to 9999-01-17T10:04:38

**For Viva 2:**

* **Participants:** Supervisor (E001), CoAdvisor (E000), President (T000), Advisor (T001)
* **FCFS Common Time Slot:  FAILS**

The schedule fails because viva 1 chose the first intersection it found(9999-01-17T00:24:38 to 9999-01-17T10:04:38), which is the only time that would work for viva 2. If viva 1 chose the second time slot that works instead of the first one(9999-12-01T01:44:41 to 9999-12-01T11:24:41), the agenda would be schedulable.

### Future Improvements

1. **Fix the FCFS Problem**: Adjust the algorithm to consider alternative valid slots for earlier viva to maximize the overall scheduling success.
2. **Enhance Property Definitions**: Expand the set of properties to cover more complex scenarios and edge cases.
3. **Extend Constraints Handling**: Implement additional constraints such as resource preferences.

### Conclusion

In Milestone 2, we successfully implemented property-based tests to validate the scheduling system. By defining key properties and using random generators, we ensured that the system adheres to the specified constraints and performs reliably under various conditions. These tests provide a strong foundation for further development and optimization of the scheduling algorithm in subsequent milestones.
