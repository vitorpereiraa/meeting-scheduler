package pj.domain.resource

import pj.domain.*

trait ResourceOps[Resource]:
  def findResourceById(resources: List[Resource])(resourceId: ResourceId): Result[Resource]
  def getNumOfTeachers(resources: List[Resource]): Int
  def getNumOfExternals(resources: List[Resource]): Int
