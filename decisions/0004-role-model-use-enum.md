# Use enum instead of sealed trait for role

* Status: PENDING
* Creator: Vitor Pereira (1191244)
* Deciders: Group
* Date: 11/04/2024
 
## Context and Problem Statement

Should we use enum instead of sealed trait for role model?

## Considered Options

```scala
enum Role(val resource: Resource):
case President(override val resource: Resource) extends Role(resource)
case Advisor(override val resource: Resource) extends Role(resource)
case CoAdvisor(override val resource: Resource) extends Role(resource)
case Supervisor(override val resource: Resource) extends Role(resource)
```

vs

```scala
sealed trait Role:
  def resource: Resource
final case class President(resource: Resource) extends Role
final case class Advisor(resource: Resource) extends Role
final case class CoAdvisor(resource: Resource) extends Role
final case class Supervisor(resource: Resource) extends Role
```