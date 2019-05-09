package com.fullfacing.keycloak4s.admin.models

final case class UserAccess(manage: Boolean,
                            impersonate: Boolean,
                            manageGroupMembership: Boolean,
                            mapRoles: Boolean,
                            view: Boolean)
