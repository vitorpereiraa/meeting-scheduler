package pj.domain.role

trait RoleOps[Role]:
  def isPresident(role: Role): Boolean
  def isAdvisor(role: Role): Boolean