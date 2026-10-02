package dev.loams.core.decision

import java.io.ByteArrayOutputStream

enum class Decision(val wire: String) {
    APPROVE("approve"),
    REJECT("reject"),
}

/**
 * What a decision proof signs (design §37 §7.3, AP0 Ruling 7). The server rebuilds these from the
 * request and compares bytes, so the encoding is fixed: keys in byte order, no whitespace,
 * integers in decimal, strings escaped as RFC 8785 does. The golden cases shared with iOS and the
 * mock are in conformance/fixtures/decision/claims.json.
 *
 * [revision] is unsigned on the wire (uint64); it is held in a Long and printed unsigned.
 */
data class DecisionClaims(
    val approvalId: String,
    val revision: Long,
    val decision: Decision,
    val iat: Long,
    val jti: String,
) {
    fun canonicalJson(): ByteArray {
        val out = StringBuilder()
        out.append("{\"approval_id\":").append(quote(approvalId))
        out.append(",\"decision\":").append(quote(decision.wire))
        out.append(",\"iat\":").append(iat)
        out.append(",\"jti\":").append(quote(jti))
        out.append(",\"revision\":").append(java.lang.Long.toUnsignedString(revision))
        out.append('}')
        return out.toString().toByteArray(Charsets.UTF_8)
    }

    companion object {
        /** RFC 8785 string escaping. */
        fun quote(s: String): String {
            val b = StringBuilder(s.length + 2).append('"')
            for (c in s) {
                when (c) {
                    '"' -> b.append("\\\"")
                    '\\' -> b.append("\\\\")
                    '\b' -> b.append("\\b")
                    '\u000c' -> b.append("\\f")
                    '\n' -> b.append("\\n")
                    '\r' -> b.append("\\r")
                    '\t' -> b.append("\\t")
                    else -> if (c < ' ') b.append("\\u%04x".format(c.code)) else b.append(c)
                }
            }
            return b.append('"').toString()
        }
    }
}

/** Compact JWS (RFC 7515) assembly for ES256, given a signer that produces DER or raw signatures. */
object Jws {
    const val DECISION_HEADER = """{"alg":"ES256","typ":"loams-decision+jws"}"""

    /** The bytes to sign: base64url(header) "." base64url(payload). */
    fun signingInput(headerJson: String, payload: ByteArray): String =
        b64(headerJson.toByteArray(Charsets.UTF_8)) + "." + b64(payload)

    fun compact(signingInput: String, rawSignature: ByteArray): String = signingInput + "." + b64(rawSignature)

    fun b64(bytes: ByteArray): String = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
}

/**
 * Converts a DER ECDSA signature (what `SHA256withECDSA` returns, on Android Keystore too) to the
 * fixed 64-byte r||s form that JWS ES256 requires (RFC 7518 §3.4).
 */
object EcdsaSignatures {
    fun derToRaw(der: ByteArray, size: Int = 32): ByteArray {
        var i = 0
        require(der[i++].toInt() == 0x30) { "not a DER sequence" }
        val len = der[i++].toInt() and 0xff
        if (len and 0x80 != 0) i += len and 0x7f // long-form length: skip its bytes
        val r = readInt(der, i).also { i = it.second }.first
        val s = readInt(der, i).first
        val out = ByteArrayOutputStream(size * 2)
        out.write(fixed(r, size))
        out.write(fixed(s, size))
        return out.toByteArray()
    }

    private fun readInt(der: ByteArray, start: Int): Pair<ByteArray, Int> {
        var i = start
        require(der[i++].toInt() == 0x02) { "not a DER integer" }
        val len = der[i++].toInt() and 0xff
        return der.copyOfRange(i, i + len) to i + len
    }

    private fun fixed(v: ByteArray, size: Int): ByteArray {
        val trimmed = v.dropWhile { it.toInt() == 0 }.toByteArray()
        require(trimmed.size <= size) { "integer too large" }
        return ByteArray(size - trimmed.size) + trimmed
    }
}
