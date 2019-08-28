package com.fullfacing.keycloak4s.auth.core

import com.fullfacing.keycloak4s.auth.core.authorization.PathAuthorization
import com.fullfacing.keycloak4s.core.Exceptions

import scala.io.{BufferedSource, Source}

object PolicyBuilders {

  /**
   * Attempts to build an Authorization object from a JSON configuration in resources.
   * Throws an Exception in case of failure.
   */
  private def attemptBuild(filename: String): BufferedSource = {
    val url = getClass.getResource(s"/$filename")

    if (url == null) {
      throw Exceptions.CONFIG_NOT_FOUND(filename)
    } else try {
      Source.fromFile(url.getPath)
    } catch {
      case th: Throwable => Logging.configSetupError(); throw th
    }
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
