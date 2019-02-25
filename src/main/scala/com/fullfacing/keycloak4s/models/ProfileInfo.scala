package models

case class ProfileInfo(
                        disabledFeatures: Option[List[String]],
                        experimentalFeatures: Option[List[String]],
                        name: Option[String],
                        previewFeatures: Option[List[String]]
                      )
