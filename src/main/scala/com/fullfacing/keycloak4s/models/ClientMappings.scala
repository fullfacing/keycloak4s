package models

case class ClientMappings(
                           client: Option[String],
                           id: Option[String],
                           mappings: Option[List[Role]]
                         )