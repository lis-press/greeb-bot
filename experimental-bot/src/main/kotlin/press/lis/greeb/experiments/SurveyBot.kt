package press.lis.greeb.experiments

import com.typesafe.config.ConfigFactory
import mu.KotlinLogging
import org.apache.commons.codec.binary.Base64
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.ApiContext
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
 */
class HammerTimeMarathonBot(botToken: String, options: DefaultBotOptions?) : TelegramLongPollingBot(options) {
    private val botTokenInternal: String = botToken
    private val logger = KotlinLogging.logger {}

    override fun getBotUsername(): String {
        return "SurveyBot"
    }

    override fun getBotToken(): String {
        return botTokenInternal
    }

    override fun onUpdateReceived(update: Update?) {
        logger.info { "Got Update $update" }

        if (update == null) {
            return
        }

        when {
            update.message?.text != null -> {
                val chatId = update.message.chatId

                // TODO Так, свитч инлайн я смог настроить здесь, пока не понимаю, как это всё

                val result = execute(SendMessage()
                        .setChatId(chatId)
//                        .setReplyMarkup(inlineKeyboardMarkup)
                        .setText(update.message?.text))


                val test = ByteBuffer.allocate(Int.SIZE_BYTES + Long.SIZE_BYTES)
                        .putInt(result.messageId)
                        .putLong(result.chatId)
                        .array()

                val inlineKeyboardMarkup = InlineKeyboardMarkup(
                        listOf(
                                listOf(
                                        InlineKeyboardButton("First").setCallbackData("F")
                                ),
                                listOf(
                                        InlineKeyboardButton("Third").setSwitchInlineQuery(
                                                Base64.encodeBase64(test)
                                                        .toString(Charset.forName("UTF8")))
                                )
                        ))

                // Don't need it actually
                execute(EditMessageReplyMarkup()
                        .setChatId(result.chatId)
                        .setMessageId(result.messageId)
                        .setReplyMarkup(inlineKeyboardMarkup))

                // TODO я могу попытаться здесь сразу отредактировать это сообщение...
                // TODO не уверен, есть ли здесь какие-то ещё интересные варианты
            }
            update.hasCallbackQuery() -> {

                val callbackQuery = update.callbackQuery

                // TODO с чего начнём? Попробуем опубликовать новый опрос в других чатах? Звучит достаточно интересно

                // I can edit the message in one or other chats, but I can't repost inline messages
                // Unless I'll do it via the bot -> I can do the bot with cross chat surveys in order to do that,
                // Looks like a quick win.

                // Another thing I would like to think about is how to send store data:
                // Easiest way is to manage the list in one centered platform + all other message list too.
                // Use one edit message as the database -> deleting all the sources after the survey finished.
                // Okay, 64 bytes for callback id makes it a bit harder
                execute(EditMessageText()
                        .setChatId(callbackQuery.message.chatId)
                        .setMessageId(callbackQuery.message.messageId)
                        .setReplyMarkup(callbackQuery.message.replyMarkup)
                        .setText(callbackQuery.message.text + "\nCallback ${callbackQuery.data} получен"))
            }
            update.hasInlineQuery() -> {


                val messageAndChatByteArray = Base64.decodeBase64(update.inlineQuery.query)
                val messageAndChatByteBuffer = ByteBuffer.wrap(messageAndChatByteArray)

                val messageId = messageAndChatByteBuffer.getInt(0)
                val chatId = messageAndChatByteBuffer.getLong(Int.SIZE_BYTES)

                // TODO duplication -> кажется, что здесь нельзя поменять опрос на тот же или всё-таки можно, я так и не понял
                val inlineKeyboardMarkup = InlineKeyboardMarkup(
                        listOf(
                                listOf(
                                        InlineKeyboardButton("First").setCallbackData("FF")
                                ),
                                listOf(
                                        InlineKeyboardButton("Third").setSwitchInlineQuery(
                                                update.inlineQuery.query)
                                )
                        ))

                val messageProcessed = execute(EditMessageReplyMarkup()
                        .setChatId(chatId)
                        .setMessageId(messageId)
                        .setReplyMarkup(inlineKeyboardMarkup))

                val results: List<InlineQueryResultArticle> = if (messageProcessed is Message) {
                    val messageProcessedInternal = messageProcessed as Message
                    listOf(InlineQueryResultArticle()
                            .setId(UUID.randomUUID().toString())
                            .setHideUrl(true)
                            .setTitle("Title test")
                            .setDescription("Description test")
                            .setInputMessageContent(InputTextMessageContent()
                                    .setMessageText(messageProcessedInternal.text)
                                    .setDisableWebPagePreview(true))
                            .setUrl("http://nfclub.tilda.ws/"))
                } else {
                    listOf()
                }

                execute(AnswerInlineQuery()
                        .setInlineQueryId(update.inlineQuery.id)
                        .setSwitchPmText("Тоже хочу завести подобный опрос")
                        .setSwitchPmParameter("Test")
//                    .setResults(listOf())
                        .setResults(results)
                )
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

    val botOptions = ApiContext.getInstance(DefaultBotOptions::class.java)

    botOptions.proxyHost = "localhost"
    botOptions.proxyPort = 1337
    botOptions.proxyType = DefaultBotOptions.ProxyType.SOCKS5

    val hammerTimeBot = HammerTimeMarathonBot(botToken, botOptions)

    botsApi.registerBot(hammerTimeBot)
}
