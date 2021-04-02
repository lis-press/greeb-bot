import org.apache.commons.codec.binary.Base64
import java.nio.ByteBuffer

val joinChatMessage = "https://t.me/joinchat/..."
val base64ChatId = joinChatMessage.replace("https://t.me/joinchat/", "")

base64ChatId

val messageAndChatByteArray = Base64.decodeBase64(base64ChatId)
val messageAndChatByteBuffer = ByteBuffer.wrap(messageAndChatByteArray)
val preChannelId = messageAndChatByteBuffer.getLong(0)

preChannelId

val chatId = ByteBuffer.allocate(16).putLong(1299920719)
Base64.encodeBase64String(chatId.array())

// https://web.telegram.org/#/im?p=c1299920719_15644435886570205487
val joinChatNewMessage = "https://web.telegram.org/#/im?p=c1299920719_15644435886570205487"
val chatIdNew = joinChatNewMessage.replace("https://web.telegram.org/#/im?p=c", "").split("_")[0]

joinChatMessage.startsWith("https://t.me/joinchat/")
chatIdNew