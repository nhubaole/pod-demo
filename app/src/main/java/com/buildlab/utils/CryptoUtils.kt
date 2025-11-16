@file:Suppress(IMPORTANT)

package com.buildlab.utils

import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.Cipher

object CryptoUtils {

    fun decrypt(cipherBytes: ByteArray, publicKey: PublicKey): ByteArray {
        return Cipher.getInstance("RSA/ECB/PKCS1Padding").run {
            init(Cipher.DECRYPT_MODE, publicKey)
            doFinal(cipherBytes)
        }
    }

    fun encrypt(plainBytes: ByteArray, privateKey: PrivateKey): ByteArray {
        return Cipher.getInstance("RSA/ECB/PKCS1Padding").run {
            init(Cipher.ENCRYPT_MODE, privateKey)
            doFinal(plainBytes)
        }
    }
}