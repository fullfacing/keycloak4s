package models

case class ClientInitialAccessCreate(
                                      count: Option[Int],
                                      expiration: Option[Int]
                                    )
