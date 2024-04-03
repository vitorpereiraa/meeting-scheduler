package pj.domain.schedule

import pj.domain.schedule.SimpleTypes.{ExternalId, TeacherId}

object RoleService extends RoleOps[Role]:
  
  def isPresident(role: Role): Boolean = role match
      case President(_) => true
      case _ => false
      
  def isAdvisor(role: Role): Boolean = role match
      case Advisor(_) => true
      case _ => false

