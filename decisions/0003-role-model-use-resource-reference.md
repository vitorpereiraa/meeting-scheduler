# Update role model to reference the resource object directly instead of id

* Status: PENDING
* Creator: Vitor Pereira (1191244)
* Deciders: Group
* Date: 10/04/2024

## Context and Problem Statement

Should we update role model to reference the resource object directly instead of id?

## Considered Options

Option 1
```scala
sealed trait Role
final case class President(id: TeacherId) extends Role
final case class Advisor(id: TeacherId) extends Role
final case class CoAdvisor(id: TeacherId | ExternalId) extends Role
final case class Supervisor(id: ExternalId) extends Role
```

Option 2
```scala
sealed trait Role
final case class President(jury: Jury) extends Role
final case class Advisor(jury: Jury) extends Role
final case class CoAdvisor(jury: Jury) extends Role
final case class Supervisor(jury: Jury) extends Role
```
