package press.lis.greeb.experiments

import org.apache.commons.codec.binary.Base64
import java.nio.ByteBuffer

/**
 * @author Aleksandr Eliseev
 */
fun main() {
    print("Nothing here yet :)")

    val messageAndChatByteArray = Base64.decodeBase64("AAAAAFI9DzwN8_90mBvbbA")
    val messageAndChatByteBuffer = ByteBuffer.wrap(messageAndChatByteArray)

    val int1 = messageAndChatByteBuffer.getInt(0)
    val long1 = messageAndChatByteBuffer.getLong(0)
    //    1379733308
    // -1001379733308
    // Looks like this could work
}