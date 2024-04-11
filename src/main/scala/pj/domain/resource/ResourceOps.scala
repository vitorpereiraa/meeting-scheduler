package pj.domain.resource

import pj.domain.*

trait ResourceOps[Resource]:
  def findResourceById(resources: List[Resource])(resourceId: ResourceId): Result[Resource]