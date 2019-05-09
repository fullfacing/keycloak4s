package com.fullfacing.keycloak4s.admin.models

final case class ProfileInfo(disabledFeatures: Option[List[String]],
                             experimentalFeatures: Option[List[String]],
                             name: Option[String],
                             previewFeatures: Option[List[String]])
