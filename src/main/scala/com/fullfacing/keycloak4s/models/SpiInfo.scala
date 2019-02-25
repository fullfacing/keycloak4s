package models

case class SpiInfo(
                    internal: Option[Boolean],
                    providers: Option[Map[_, _]]
                  )
