package com.fullfacing.keycloak4s.models

final case class ProfileInfo(disabledFeatures: Option[List[String]],
                             experimentalFeatures: Option[List[String]],
                             name: Option[String],
                             previewFeatures: Option[List[String]])
