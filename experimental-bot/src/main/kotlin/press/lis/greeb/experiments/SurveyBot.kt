package press.lis.greeb.experiments

import com.typesafe.config.ConfigFactory
import mu.KotlinLogging
import org.apache.commons.codec.binary.Base64
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.AnswerInlineQuery
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageReplyMarkup
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.inlinequery.inputmessagecontent.InputTextMessageContent
import org.telegram.telegrambots.meta.api.objects.inlinequery.result.InlineQueryResultArticle
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import java.nio.ByteBuffer
import java.nio.charset.Charset
import java.util.*

/**
 * @author Aleksandr Eliseev
 *
 */
class HammerTimeMarathonBot(botToken: String) : TelegramLongPollingBot() {
    // TODO Восстановить бы контекст, чего мне сейчас
    private val botTokenInternal: String = botToken
    private val logger = KotlinLogging.logger {}

    override fun getBotUsername(): String {
        return "SurveyBot"
    }

    override fun getBotToken(): String {
        return botTokenInternal
    }

    private fun getInlineKeyboard(switchInlineQuery: String): InlineKeyboardMarkup {
        return InlineKeyboardMarkup(
                listOf(
                        listOf(
                                InlineKeyboardButton("First").setCallbackData(System.currentTimeMillis().toString())
                        ),
                        listOf(
                                InlineKeyboardButton("Third").setSwitchInlineQuery(switchInlineQuery)
                        )
                ))
    }

    // TODO better to make converter to the separate type
    private fun toBase64String(int1: Int, long1: Long): String {
        val byteArray = ByteBuffer.allocate(Int.SIZE_BYTES + Long.SIZE_BYTES)
                .putInt(int1)
                .putLong(long1)
                .array()

        return Base64.encodeBase64(byteArray).toString(Charset.forName("UTF8"))
    }

    private fun toBase64String(int1: Int, long1: Long, int2: Int): String {
        val byteArray = ByteBuffer.allocate(Int.SIZE_BYTES + Long.SIZE_BYTES + Int.SIZE_BYTES)
                .putInt(int1)
                .putLong(long1)
                .putInt(int2)
                .array()

        return Base64.encodeBase64(byteArray).toString(Charset.forName("UTF8"))
    }

    private fun base64toIntLong(base64: String): Pair<Int, Long> {
        val messageAndChatByteArray = Base64.decodeBase64(base64)
        val messageAndChatByteBuffer = ByteBuffer.wrap(messageAndChatByteArray)

        val int1 = messageAndChatByteBuffer.getInt(0)
        val long1 = messageAndChatByteBuffer.getLong(Int.SIZE_BYTES)

        return Pair(int1, long1)
    }

    private fun base64toIntLongInt(base64: String): Triple<Int, Long, Int> {
        val messageAndChatByteArray = Base64.decodeBase64(base64)
        val messageAndChatByteBuffer = ByteBuffer.wrap(messageAndChatByteArray)

        val int1 = messageAndChatByteBuffer.getInt(0)
        val long1 = messageAndChatByteBuffer.getLong(Int.SIZE_BYTES)
        val int2 = messageAndChatByteBuffer.getInt(Int.SIZE_BYTES + Long.SIZE_BYTES)

        return Triple(int1, long1, int2)
    }

    private fun getMessage(messageId: Int, chatId: Long, switchInlineQuery: String): Message {
        val inlineKeyboardMarkup = getInlineKeyboard(
                switchInlineQuery = switchInlineQuery)

        val messageProcessed = execute(EditMessageReplyMarkup()
                .setChatId(chatId)
                .setMessageId(messageId)
                .setReplyMarkup(inlineKeyboardMarkup))

        // TODO I strongly believe that there should be a Message
        return messageProcessed as Message
    }

    override fun onUpdateReceived(update: Update?) {
        logger.info { "Got Update $update" }

        if (update == null) {
            return
        }

        when {
            update.message?.text != null -> {
                val chatId = update.message.chatId

                val result = execute(SendMessage()
                        .setChatId(chatId)
                        .setText(update.message?.text))

                val inlineKeyboardMarkup = getInlineKeyboard(
                        switchInlineQuery = toBase64String(result.messageId, result.chatId))

                // Need to reference the current message in reply markup
                execute(EditMessageReplyMarkup()
                        .setChatId(result.chatId)
                        .setMessageId(result.messageId)
                        .setReplyMarkup(inlineKeyboardMarkup))
            }
            update.hasCallbackQuery() -> {

                val callbackQuery = update.callbackQuery

                if (callbackQuery.message != null) {
                    // It means, it's callback from the chat with bot
                    execute(EditMessageText()
                            .setChatId(callbackQuery.message.chatId)
                            .setMessageId(callbackQuery.message.messageId)
                            .setReplyMarkup(callbackQuery.message.replyMarkup)
                            .setText(callbackQuery.message.text + "\nCallback ${callbackQuery.data} получен"))
                } else {
                    // It means, it's callback from other chat...
                    val (messageId, chatId, optionId) = base64toIntLongInt(callbackQuery.data)

                    val messageProcessed = getMessage(messageId = messageId,
                            chatId = chatId,
                            switchInlineQuery = update.callbackQuery.data)

                    // TODO --> (.+)( --> .*)? найти, проверить, есть ли в последнем списке

                    val lineToEdit =
                            messageProcessed.text.lines().filter { it.startsWith("-->") }[optionId]
                    val splitMessage = lineToEdit.split(" #> ")
                    val splitNames =
                            splitMessage
                                    .elementAtOrNull(1)?.split(" ")?.toSet() ?: emptySet()

                    val finalSet = if (splitNames.contains(callbackQuery.from.userName)) {
                        splitNames.minus(callbackQuery.from.userName)
                    } else {
                        splitNames.plus(callbackQuery.from.userName)
                    }

                    val newMessage =
                            messageProcessed.text.replace(lineToEdit,
                                    "${splitMessage[0]} #> ${finalSet.joinToString(separator = " ")}")

                    execute(EditMessageText()
                            .setChatId(messageProcessed.chatId)
                            .setMessageId(messageProcessed.messageId)
                            .setReplyMarkup(messageProcessed.replyMarkup)
                            .setText(newMessage))

                    val message = newMessage

                    val mainText = message
                            .lines()
                            .filterNot { it.startsWith("--") }
                            .joinToString("\n")

                    var options = emptyList<Pair<String, Int>>()
                    var inlineIds = emptyList<String>()

                    message
                            .lines()
                            .filter { it.startsWith("--") }
                            .forEach {
                                when {
                                    it[2] == '>' -> {
                                        val split = it.substring(4).split(" #> ")
                                        options = options.plus(Pair(split[0],
                                                split.getOrNull(1)?.split(" ")
                                                        ?.filter { inner -> inner.isNotBlank() }?.size ?: 0))
                                    }
                                    it[2] == '#' -> {
                                        inlineIds = inlineIds.plus(it.substring(4))
                                    }
                                }
                            }
                    val optionsString = options.joinToString("\n") { "`${it.first}` (${it.second})" }
                    val resultingMessage = "$mainText\n\n$optionsString"

                    val generatedButtons = options
                            .map { it.first }
                            .mapIndexed { id, optionText ->
                                val returnData = toBase64String(messageId, chatId, id)

                                listOf(InlineKeyboardButton(optionText)
                                        .setCallbackData(returnData))
                            }.toList()

                    val replyMarkup = InlineKeyboardMarkup(generatedButtons)

//                    // TODO Learn to preprocess messages in order to provide the correct markup and broadcast
                    inlineIds.forEach {
                        execute(EditMessageText()
                                .setInlineMessageId(it)
                                .setText(resultingMessage)
                                .setReplyMarkup(replyMarkup)
                                .enableMarkdown(true))
                    }
                }
            }
            update.hasInlineQuery() -> {

                // TODO seems like case without message is not processed correctly
                var results: List<InlineQueryResultArticle>

                try {
                    val (messageId, chatId) = base64toIntLong(update.inlineQuery.query)

                    val messageProcessed = getMessage(messageId = messageId,
                            chatId = chatId,
                            switchInlineQuery = update.inlineQuery.query)

                    val notAnswer = InlineQueryResultArticle()
                            .setId(UUID.randomUUID().toString())
                            .setHideUrl(true)
                            .setTitle("Title test")
                            .setDescription("Description test")
                            .setUrl("http://nfclub.tilda.ws/")

                    val message = messageProcessed.text

                    val mainText = message
                            .lines()
                            .filterNot { it.startsWith("--") }
                            .joinToString("\n")

                    var options = emptyList<Pair<String, Int>>()
                    val inlineIds = emptyList<String>()

                    message
                            .lines()
                            .filter { it.startsWith("--") }
                            .forEach {
                                when {
                                    it[2] == '>' -> {
                                        val split = it.substring(4).split(" #> ")
                                        options = options.plus(Pair(split[0],
                                                split.getOrNull(1)?.split(" ")
                                                        ?.filter { inner -> inner.isNotBlank() }?.size ?: 0))
                                    }
                                    it[2] == '#' -> {
                                        inlineIds.plus(it.substring(4))
                                    }
                                }
                            }
                    val optionsString = options.joinToString("\n") { "`${it.first}` (${it.second})" }
                    val resultingMessage = "$mainText\n\n$optionsString"

                    if (options.isNotEmpty()) {
                        val generatedButtons = options
                                .map { it.first }
                                .mapIndexed { id, optionText ->
                                    val returnData = toBase64String(messageId, chatId, id)

                                    listOf(InlineKeyboardButton(optionText)
                                            .setCallbackData(returnData))
                                }.toList()

                        notAnswer.replyMarkup = InlineKeyboardMarkup(generatedButtons)
                    }

                    notAnswer.inputMessageContent = InputTextMessageContent()
                            .setMessageText(resultingMessage)
                            .enableMarkdown(true)
                            .setDisableWebPagePreview(true)

                    results = listOf(notAnswer)
                } catch (e: Exception) {
                    // TODO not sure it's best to process it via Exception
                    logger.warn("Not a valid callback id", e)
                    results = listOf()
                }

                execute(AnswerInlineQuery()
                        .setInlineQueryId(update.inlineQuery.id)
                        .setSwitchPmText("Тоже хочу завести подобный опрос")
                        .setSwitchPmParameter("Test")
                        .setResults(results)
                )
            }
            update.hasChosenInlineQuery() -> {
                val inlineMessageId = update.chosenInlineQuery.inlineMessageId
                val (messageId, chatId) = base64toIntLong(update.chosenInlineQuery.query)

                val message = getMessage(messageId = messageId,
                        chatId = chatId,
                        switchInlineQuery = update.chosenInlineQuery.query)

                execute(EditMessageText()
                        .setChatId(message.chatId)
                        .setMessageId(message.messageId)
                        .setReplyMarkup(message.replyMarkup)
                        .setText(message.text + "\n--# $inlineMessageId"))
            }
            else -> {
                print(1)
            }
        }
    }
}

fun main() {
    println("Started")

    val botToken = ConfigFactory.load().getString("bot.token")
    ApiContextInitializer.init()

    val botsApi = TelegramBotsApi()

    val hammerTimeBot = HammerTimeMarathonBot(botToken)

    botsApi.registerBot(hammerTimeBot)
}
