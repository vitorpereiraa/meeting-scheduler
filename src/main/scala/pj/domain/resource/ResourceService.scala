package pj.domain.resource

import pj.domain.*
import pj.domain.DomainError.ResourceNotFound

object ResourceService extends ResourceOps[Resource]:

  override def findResourceById(resources: List[Resource])(resourceId: ResourceId): Result[Resource] =
    resources.find(_.id == resourceId)
      .fold(Left(ResourceNotFound(resourceId)))(r => Right(r))

  override def getNumOfTeachers(resources: List[Resource]): Int =
    resources.foldLeft[Int](0)(
      (tqty, r) => r match
        case _: Teacher => tqty + 1
        case _ => tqty
    )

  override def getNumOfExternals(resources: List[Resource]): Int =
    resources.foldLeft[Int](0)(
      (eqty, r) => r match
        case _: External => eqty + 1
        case _ => eqty
    )