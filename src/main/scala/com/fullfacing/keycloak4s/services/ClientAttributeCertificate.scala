package com.fullfacing.keycloak4s.services

import java.io.File

import com.fullfacing.apollo.core.Predef.AsyncApolloResponse
import com.fullfacing.keycloak4s.SttpClient
import com.fullfacing.keycloak4s.models._
import com.softwaremill.sttp.Uri.QueryFragment.KeyValue

import scala.collection.immutable.Seq

object ClientAttributeCertificate {

  /**
   * Get key info.
   *
   * @param attribute
   * @param clientId  ID of client (not client-id).
   * @param realm     Name of the Realm.
   * @return
   */
  def getKeyInfo(attribute: String, clientId: String, realm: String): AsyncApolloResponse[Certificate] = {
    val path = Seq(realm, "clients", clientId, "certificates", attribute)
    SttpClient.get(path)
  }

  /**
   * Get a keystore file for the client, containing private key and public certificate.
   *
   * @param attribute
   * @param clientId  ID of client (not client-id).
   * @param realm     Name of the Realm.
   * @param config    Keystore configuration.
   * @return
   */
  def getKeystoreFile(attribute: String, clientId: String, realm: String, config: KeyStoreConfig): AsyncApolloResponse[Seq[Byte]] = {
    //TODO Call produces an octet-stream, reminder to look into converting the bytes into a File instead.
    val path = Seq(realm, "clients", clientId, "certificates", attribute, "download")
    SttpClient.post(config, path)
  }

  /**
   * Generate a new certificate with new key pair
   *
   * @param attribute
   * @param clientId  ID of client (not client-id).
   * @param realm     Name of the Realm.
   * @return
   */
  def generateNewCertificate(attribute: String, clientId: String, realm: String): AsyncApolloResponse[Certificate] = {
    val path = Seq(realm, "clients", clientId, "certificates", attribute, "generate")
    SttpClient.post(path, queries = Seq.empty[KeyValue])
  }

  /**
   * Generates a keypair and certificate and serves the private key in a specified keystore format.
   *
   * @param attribute
   * @param clientId  ID of client (not client-id).
   * @param realm     Name of the Realm.
   * @param config    Keystore configuration.
   * @return
   */
  def generateAndDownloadNewCertificate(attribute: String, clientId: String, realm: String, config: KeyStoreConfig): AsyncApolloResponse[Seq[Byte]] = {
    //TODO Call produces an octet-stream, reminder to look into converting the bytes into a File instead.
    val path = Seq(realm, "clients", clientId, "certificates", attribute, "generate-and-download")
    SttpClient.post(config, path)
  }

  /**
   * Upload certificate and private key.
   *
   * @param attribute
   * @param clientId  ID of client (not client-id).
   * @param realm     Name of the Realm.
   * @param file
   * @return
   */
  def uploadCertificateWithPrivateKey(attribute: String, clientId: String, realm: String, file: File): AsyncApolloResponse[Certificate] = {
    //TODO Add functionality to SttpClient for multipart form-data.
    val path = Seq(realm, "clients", clientId, "certificates", attribute, "upload")
    SttpClient.post(file, path)
  }

  /**
   * Upload only certificate, not private key.
   *
   * @param attribute
   * @param clientId  ID of client (not client-id).
   * @param realm     Name of the Realm.
   * @param file
   * @return
   */
  def uploadCertificateWithoutPrivateKey(attribute: String, clientId: String, realm: String, file: File): AsyncApolloResponse[Certificate] = {
    //TODO Add functionality to SttpClient for multipart form-data.
    val path = Seq(realm, "clients", clientId, "certificates", attribute, "upload-certificate")
    SttpClient.post(file, path)
  }
}
