package models

case class ComponentType[A](
                             helpText: Option[String],
                             id: Option[String],
                             metadata: Option[Map[_, _]],
                             properties: Option[ConfigProperty[A]]
                           )