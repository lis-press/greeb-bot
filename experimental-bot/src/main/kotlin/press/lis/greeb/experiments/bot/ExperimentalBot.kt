package press.lis.greeb.experiments.bot

import mu.KotlinLogging
import org.apache.commons.codec.binary.Base64
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import press.lis.greeb.spreadsheets.SheetsClient
import java.nio.ByteBuffer


/**
 * @author Aleksandr Eliseev
 */
class ExperimentalBot(botToken: String, options: DefaultBotOptions?) : TelegramLongPollingBot(options) {
    private val botTokenInternal: String = botToken
    private val logger = KotlinLogging.logger {}
    private val spreadSheetService = SheetsClient.sheetService
    private val experimentalSheetId = "14_EFQnHaewEcLL3aUkMktbdCbmOVXFJd-dGajBX6SWM"

    private lateinit var header: List<Any>
    private lateinit var chats: List<Any>

    private fun getBugHuntSheet(): Iterable<IndexedValue<List<Any>>> {
        val response = spreadSheetService.spreadsheets().values()
                .get(experimentalSheetId,
                        "A:AM")
                .execute()

        return response.getValues().withIndex()
    }


    private fun initializeIndex() {
        val bugHuntSheet = getBugHuntSheet()
        val iterator = bugHuntSheet.iterator()
        header = iterator.next().value
        chats = iterator.next().value
    }


    override fun getBotUsername(): String {
        return "ExperimentalBot"
    }

    override fun getBotToken(): String {
        return botTokenInternal
    }

    // TODO Bot needs to be Admin in any chat ->
    override fun onUpdateReceived(update: Update?) {
        logger.info { "Got Update $update" }

        // Used only for me, ignore anyone else, to ensure nothing wrong happening
        if (update?.message?.text != null && update.message.from.userName == "eliseealex") {

            if (update.message.text.length != 1) {
                val sendMessage = SendMessage()
                        .setChatId(update.message.chatId)
                        .setText("No way!")

                execute(sendMessage)
            }

            initializeIndex()

            val joinChatMessage = chats[update.message.text[0] - 'A'].toString()   // TODO two letters case
            val base64ChatId = joinChatMessage.replace("https://t.me/joinchat/", "")

            val messageAndChatByteArray = Base64.decodeBase64(base64ChatId)
            val messageAndChatByteBuffer = ByteBuffer.wrap(messageAndChatByteArray)
            val preChannelId = messageAndChatByteBuffer.getLong(0)

            val channelId = (1000000000000 + preChannelId) * -1

            val sendMessage = SendMessage()
                    .setChatId(channelId)
                    .setText("Hello, world")

            execute(sendMessage)
        }
    }
}