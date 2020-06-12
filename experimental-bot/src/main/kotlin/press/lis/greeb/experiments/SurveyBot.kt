package press.lis.greeb.experiments

import com.typesafe.config.ConfigFactory
import mu.KotlinLogging
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.ApiContext
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton
import press.lis.greeb.spreadsheets.SheetsClient

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

        if (update.message?.text != null) {
            val chatId = update.message.chatId

            val inlineKeyboardMarkup = InlineKeyboardMarkup(
                    listOf(
                            listOf(
                                    InlineKeyboardButton("First").setCallbackData("F")
                            ),
                            listOf(
                                    InlineKeyboardButton("Third").setCallbackData("T")
                            )
                    ))

            execute(SendMessage()
                    .setChatId(chatId)
                    .setReplyMarkup(inlineKeyboardMarkup)
                    .setText("Сообщение!"))
        } else if (update.hasCallbackQuery()) {

            val callbackQuery = update.callbackQuery

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
