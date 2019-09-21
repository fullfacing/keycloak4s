package com.fullfacing.keycloak4s.auth.core

import com.fullfacing.keycloak4s.auth.core.authorization.PathAuthorization

import scala.io.{BufferedSource, Source}

object PolicyBuilders {

  /**
   * Attempts to build an Authorization object from a JSON configuration in resources.
   * Throws an Exception in case of failure.
   */
  private def attemptBuild(filename: String): BufferedSource = {
    Source.fromResource(filename)
  }

  /**
   * Builds a PathAuthorization object from a JSON configuration file using a path structure.
   * The JSON file must be located in the resources directory.
   *
   * @param filename The file name and extension of the JSON configuration inside the Resources directory.
   *                 Example: config.json
   */
  def buildPathAuthorization(filename: String): PathAuthorization = {
    val source = attemptBuild(filename)
    PathAuthorization(source.mkString.stripMargin)
  }
}
