package models

case class GlobalRequestResult(
                                failedRequests: Option[List[String]],
                                successRequests: Option[List[String]]
                              )
