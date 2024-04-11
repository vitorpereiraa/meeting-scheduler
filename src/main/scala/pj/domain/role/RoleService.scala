package pj.domain.role

import pj.domain.Role
import pj.domain.Role.{Advisor, President}

object RoleService extends RoleOps[Role]:
  
  override def isPresident(role: Role): Boolean = role match
      case President(_) => true
      case _ => false
      
  override def isAdvisor(role: Role): Boolean = role match
      case Advisor(_) => true
      case _ => false

