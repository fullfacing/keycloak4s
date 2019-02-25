package models

case class MultivaluedHashMap(
                               empty: Option[Boolean],
                               loadFactor: Option[Float],
                               threshold: Option[Int]
                             )
