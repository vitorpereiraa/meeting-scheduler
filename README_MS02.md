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


## Future Improvements


## Conclusion

