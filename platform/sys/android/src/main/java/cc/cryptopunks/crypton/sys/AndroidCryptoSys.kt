package cc.cryptopunks.crypton.sys

import android.util.Base64
import cc.cryptopunks.crypton.aesgcm.withAes
import cc.cryptopunks.crypton.context.AesGcm
import cc.cryptopunks.crypton.context.Crypto
import java.io.InputStream
import java.io.OutputStream

object AndroidCryptoSys : Crypto.Sys {

    override fun encodeBase64(
        array: ByteArray
    ) = Base64.encodeToString(array, Base64.NO_PADDING)!!

    override fun decodeBase64(
        string: String
    ) = Base64.decode(string, Base64.NO_PADDING)!!

    override fun transform(
        stream: InputStream,
        secure: AesGcm.Secure,
        mode: Crypto.Mode
    ) = stream.withAes(secure.iv, secure.key, mode.id)

    override fun transform(
        stream: OutputStream,
        secure: AesGcm.Secure,
        mode: Crypto.Mode,
    ) = stream.withAes(secure.iv, secure.key, mode.id)
}