package licensing

import java.nio.file.{Files, Paths}
import java.util.Base64

import xyz.wiedenhoeft.scalacrypt._

import scala.util.{Failure, Success}

object EncryptKeyProvider {
  private[this] val encryptKeyDir = "./util/licenseGenerator/src/main/resources/keys/"
  private[this] val encryptKeyFilename = "license-encrypt.key"
  lazy val encryptKey = {
    val encryptKeyContent = Files.readAllBytes(Paths.get(encryptKeyDir, encryptKeyFilename))
    val encryptKeyContentDecoded = Base64.getDecoder.decode(encryptKeyContent).toSeq
    encryptKeyContentDecoded.toKey[RSAKey] match {
      case Success(content) => content
      case Failure(x) => throw x
    }
  }

  def generateKeys() = {
    val privateKey = Key.generate[RSAKey]
    val privateKeyContent = Base64.getEncoder.encode(privateKey.bytes.toArray)
    val privateKeyContentDecoded = Base64.getDecoder.decode(privateKeyContent).toSeq
    val newPrivateKey = privateKeyContentDecoded.toKey[RSAKey]
    println("Private Key:")
    println(privateKeyContent.map(_.toChar).mkString)
    Files.write(Paths.get("./conf", DecryptKeyProvider.decryptKeyFilename), privateKeyContent)

    val publicKey = privateKey.publicKey
    val publicKeyContent = Base64.getEncoder.encode(publicKey.bytes.toArray)
    val publicKeyContentDecoded = Base64.getDecoder.decode(publicKeyContent).toSeq
    val newPublicKey = publicKeyContentDecoded.toKey[RSAKey]
    println("Public Key:")
    println(publicKeyContent.map(_.toChar).mkString)
    Files.write(Paths.get(encryptKeyDir, encryptKeyFilename), publicKeyContent)
  }
}