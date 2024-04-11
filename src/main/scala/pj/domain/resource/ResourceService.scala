package pj.domain.resource

import pj.domain.*
import pj.domain.DomainError.ResourceNotFound

object ResourceService extends ResourceOps[Resource]:

  override def findResourceById(resources: List[Resource])(resourceId: ResourceId): Result[Resource] =
    resources.find(_.id == resourceId)
      .fold(Left(ResourceNotFound(resourceId)))(r => Right(r))