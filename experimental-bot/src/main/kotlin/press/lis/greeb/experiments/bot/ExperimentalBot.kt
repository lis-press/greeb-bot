package press.lis.greeb.experiments.bot

import mu.KotlinLogging
import org.apache.commons.codec.binary.Base64
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
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
    private lateinit var rest: List<IndexedValue<List<Any>>>
    // Array lists are used at the execution time, but they are hidden

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
        rest = iterator.asSequence().toList()
    }


    override fun getBotUsername(): String {
        return "ExperimentalBot"
    }

    override fun getBotToken(): String {
        return botTokenInternal
    }

    // TODO Bot needs to be Admin in any chat ->
    // TODO
    override fun onUpdateReceived(update: Update?) {
        logger.info { "Got Update $update" }

        // Used only for me, ignore anyone else, to ensure nothing wrong happening
        if (update?.message?.text != null && update.message.from.userName == "eliseealex") {

            val columnPattern = "[A-Z]".toRegex() // TODO two letters case
            val columnRowPattern = "[A-Z]([0-9]+)".toRegex()  // TODO could be the single airflow patter

            when {
                columnPattern.matches(update.message.text) -> {

                    initializeIndex()

                    var lastDone: Int? = null
                    var nextTaskText: String? = null

                    for (row in rest) {
                        if (row.value.size > update.message.text[0] - 'A' &&
                                row.value[update.message.text[0] - 'A'] == "Прошел") {
                            lastDone = row.index
                        }
                        if (row.index == (lastDone ?: 0) + 1) {
                            nextTaskText = row.value[2].toString()
                        }
                    }

                    val joinChatMessage = chats[update.message.text[0] - 'A'].toString()   // TODO two letters case
                    val base64ChatId = joinChatMessage.replace("https://t.me/joinchat/", "")

                    val messageAndChatByteArray = Base64.decodeBase64(base64ChatId)
                    val messageAndChatByteBuffer = ByteBuffer.wrap(messageAndChatByteArray)
                    val preChannelId = messageAndChatByteBuffer.getLong(0)

                    val channelId = (1000000000000 + preChannelId) * -1

                    if (nextTaskText != null) {
                        nextTaskText.split("\n--new-message--\n").forEach { messageText ->
                            if (messageText.contains(".jpg")) { // TODO just to test, need better regex
                                execute(SendPhoto()
                                        .setChatId(channelId)
                                        .setPhoto(messageText))
                            } else {
                                execute(SendMessage()
                                        .setChatId(channelId)
                                        .setText(messageText))
                            }
                        }
                    } else {
                        execute(SendMessage()
                                .setChatId(channelId)
                                .setText("Здорово! Ты прошёл все доступные задания!"))
                    }
                }
                columnRowPattern.matches(update.message.text) -> {
                    val spreadsheetRowNumber = columnRowPattern.find(update.message.text)!!.groupValues[1].toInt()
                    // 1 for spreadsheet 1-starting arrays and 2 for chats and heading
                    val localRowNumber = spreadsheetRowNumber - 3

                    initializeIndex()

                    val joinChatMessage = chats[update.message.text[0] - 'A'].toString()   // TODO two letters case
                    val base64ChatId = joinChatMessage.replace("https://t.me/joinchat/", "")

                    val messageAndChatByteArray = Base64.decodeBase64(base64ChatId)
                    val messageAndChatByteBuffer = ByteBuffer.wrap(messageAndChatByteArray)
                    val preChannelId = messageAndChatByteBuffer.getLong(0)

                    val channelId = (1000000000000 + preChannelId) * -1

                    val messagesText = rest[localRowNumber].value[2].toString()

                    messagesText.split("\n--new-message--\n").forEach { messageText ->
                        if (messageText.contains(".jpg")) { // TODO just to test, need better regex
                            execute(SendPhoto()
                                    .setChatId(channelId)
                                    .setPhoto(messageText))
                        } else {
                            execute(SendMessage()
                                    .setChatId(channelId)
                                    .setText(messageText))
                        }
                    }
                }
                else -> {
                    val sendMessage = SendMessage()
                            .setChatId(update.message.chatId)
                            .setText("Не знаю такой команды, я могу отсылать сообщение человеку" +
                                    "по его колонке в спредшите и конкретное задание!")

                    execute(sendMessage)
                }
            }

            // TODO научиться сохранять выполняемое задание
            // TODO научиться переключать задание в проверку...
            // TODO научиться ученику завершать задание...
            // TODO добавить валидацию текущего задания
            // TODO дать возможность выбирать уровень для ученика
            // TODO сделать проверку админов через spreadsheet по имени
            // TODO обработать кейс, когда у ученика не осталось больше заданий
            // TODO попытаться запилить обработать случай, когда у ученика не осталось больше заданий
            // TODO добавление ученика/создание группы
        }
    }
}