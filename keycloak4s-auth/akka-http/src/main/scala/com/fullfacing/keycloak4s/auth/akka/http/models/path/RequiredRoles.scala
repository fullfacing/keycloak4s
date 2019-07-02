package com.fullfacing.keycloak4s.auth.akka.http.models.path

import com.fullfacing.keycloak4s.auth.akka.http.Logging
import org.json4s.JsonAST.{JArray, JObject, JString, JValue}

sealed trait RequiredRoles

final case class And(and: List[Either[RequiredRoles, String]]) extends RequiredRoles
final case class Or(or: List[Either[RequiredRoles, String]])  extends RequiredRoles

object RequiredRoles {

  def apply(roles: JObject): RequiredRoles = convert(roles)

  def toEithers(list: List[JValue]): List[Either[RequiredRoles, String]] = list.collect {
    case JString(s)   => Right(s)
    case obj: JObject => Left(convert(obj))
  }

  def convert(obj: JObject): RequiredRoles = obj.obj.headOption.collect {
    case ("and", JArray(arr)) => And(toEithers(arr))
    case ("or", JArray(arr))  => Or(toEithers(arr))
  }.getOrElse(Logging.authConfigInitException())
}