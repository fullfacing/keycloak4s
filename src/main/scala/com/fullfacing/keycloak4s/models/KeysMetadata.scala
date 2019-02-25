package models

case class KeysMetadata(
                         active: Option[Map[_, _]],
                         keys: Option[List[KeysMetadataRepresentationKey]]
                       )
