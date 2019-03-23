package com.nitronapps.brsc_diary.Others

import android.util.Base64
import android.util.Log
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AESCipher() {
    companion object {
        private val vector = "NitronAppBRSCDiaryAPP"
        private val iv = IvParameterSpec(vector.toByteArray(Charsets.UTF_8))
        private val cipher = Cipher.getInstance("AES/CBC/NoPadding")

        fun encrypt(text: String, key: String): String {
            Log.w("key", key)
            Log.w("text", text)
            val keyBytes = key.toByteArray().sliceArray(IntRange(0, 15))
            val secretKey = SecretKeySpec(keyBytes, "AES")
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val result = Base64.encodeToString(cipher.doFinal(text.toByteArray()), Base64.DEFAULT)
            Log.w("textE", result)
            return result
        }

        public fun decrypt(text: String, key: String): String {
            Log.w("key", key)
            Log.w("text", text)
            val keyBytes = key.toByteArray().sliceArray(IntRange(0, 15))
            val secretKey = SecretKeySpec(keyBytes, "AES")
            cipher.init(Cipher.DECRYPT_MODE, secretKey)

            return String(cipher.doFinal(Base64.decode(text, Base64.DEFAULT)))
        }
    }
}