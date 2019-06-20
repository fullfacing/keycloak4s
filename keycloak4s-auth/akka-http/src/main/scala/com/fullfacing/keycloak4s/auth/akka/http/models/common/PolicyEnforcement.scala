package com.fullfacing.keycloak4s.auth.akka.http.models.common

import com.fullfacing.keycloak4s.core.models.enums.{PolicyEnforcementMode, PolicyEnforcementModes}

trait PolicyEnforcement {

  val enforcementMode: PolicyEnforcementMode

  /** Determines what to do when there is no matching policy for the request */
  def noMatchingPolicy(): Boolean = enforcementMode match {
    case PolicyEnforcementModes.Enforcing  => false
    case PolicyEnforcementModes.Permissive => true
    case PolicyEnforcementModes.Disabled   => true
  }

  def policyDisabled(): Boolean = {
    enforcementMode == PolicyEnforcementModes.Disabled
  }
}
