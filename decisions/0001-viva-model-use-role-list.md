# Viva Model

* Status: PENDING
* Creator: Vitor Pereira (1191244)
* Deciders: Group
* Date: 27/03/2024

## Context and Problem Statement

In the viva model should we use a field with a set of roles or have a president and advisor field.

## Considered Options

Option 1
```scala
final case class Viva(student: Student,
                      title: Title,
                      president: Role.President,
                      advisor: Role.Advisor,
                      coAdvisors: List[Role.CoAdvisor],
                      superVisors: List[Role.Supervisor])
```

Option 2
```scala
final case class Viva(
                        student: Student,
                        title: Title,
                        jury: List[Role] 
                      )
```

## Constraints

Each teacher can have only one role.

The roles associated with the viva are:
* one jury President (mandatory)
* one Advisor (mandatory)
* zero or more Co-advisors (optional)
* zero or more Supervisors (optional)

## Pros and Cons of the Options

### Option 1

* Good, because there is no need to validate the rules for roles in a viva. 
* Bad, because there is need to validate that president != advisor != all CoAdvisors != all Supervisors

### Option 2

* Good, because there is no need to validate that president != advisor != all CoAdvisors != all Supervisors
* Bad, because there is need to validate if the Set has at least one element with role president and advisor

## Decision Outcome

Chosen[Vitor] option 2, because it's more flexible, and it's easier to check if a set has certain elements than validate equality across 4 fields.

### Positive Consequences

* Easier to implement.
* Better for maintainability (in case new roles are required in the jury) 

### Negative Consequences

* The viva model does not express the domain as much.