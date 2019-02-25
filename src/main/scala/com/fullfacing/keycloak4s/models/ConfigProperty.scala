package models

case class ConfigProperty[A](
                              defaultValue: A,
                              helpText: Option[String],
                              label: Option[String],
                              name: Option[String],
                              options: Option[List[String]],
                              secret: Option[Boolean],
                              `type`: Option[String]
                            )
