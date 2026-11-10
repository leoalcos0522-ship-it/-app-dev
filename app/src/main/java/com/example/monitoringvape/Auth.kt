package com.example.monitoringvape

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * App password. The password itself is never saved, only a salted PBKDF2 hash.
 */
class Auth(context: Context) {
    private val prefs = context.getSharedPreferences("vapepos_auth", Context.MODE_PRIVATE)

    fun hasPassword(): Boolean = prefs.getString("hash", null) != null

    fun setPassword(password: String) {
        val salt = ByteArray(16).also { SecureRandom().nextBytes(it) }
        prefs.edit()
            .putString("salt", Base64.encodeToString(salt, Base64.NO_WRAP))
            .putString("hash", Base64.encodeToString(hash(password, salt), Base64.NO_WRAP))
            .apply()
    }

    fun check(password: String): Boolean {
        val saltText = prefs.getString("salt", null) ?: return false
        val hashText = prefs.getString("hash", null) ?: return false
        val salt = Base64.decode(saltText, Base64.NO_WRAP)
        val expected = Base64.decode(hashText, Base64.NO_WRAP)
        return MessageDigest.isEqual(expected, hash(password, salt))
    }

    private fun hash(password: String, salt: ByteArray): ByteArray {
        val spec = PBEKeySpec(password.toCharArray(), salt, 10_000, 160)
        return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1").generateSecret(spec).encoded
    }
}
