package press.lis.greeb.bug_hunt

import com.typesafe.config.ConfigFactory
import mu.KotlinLogging
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.ApiContext
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import press.lis.greeb.spreadsheets.SheetsClient

/**
 * @author Aleksandr Eliseev
 */
class BugHuntBot(botToken: String, options: DefaultBotOptions?) : TelegramLongPollingBot(options) {
    private val botTokenInternal: String = botToken
    private val logger = KotlinLogging.logger {}
    private val spreadSheetService = SheetsClient.sheetService
    private val bugHuntSheetId = "1u1pQx3RqqOFX-rr3Wuajyts_ufCeIQ21Mu0ndXCdv2M"

    private val currentIterator: Iterator<List<Any>> = getBugHuntSheet().iterator()


    override fun getBotUsername(): String {
        return "BugHuntBot"
    }

    override fun getBotToken(): String {
        return botTokenInternal
    }

    override fun onUpdateReceived(update: Update?) {
        logger.info { "Got Update $update" }

        if (update?.message?.text != null) {
            val inputText = update.message.text

            val userChatId = update.message.chatId

            val sendMessage = SendMessage() // Create a SendMessage object with mandatory fields
                    .setChatId(userChatId)

            when (inputText) {
                "nw", "тц", "]", "ъ" -> sendMessage.text = "Scheduling to the next week"
                "nm", "ть", "[", "х" -> sendMessage.text = "Scheduling to the next month"
                else -> {       // TODO -> here I can make a comment
                    logger.debug("Got message: {}", update)
                    val nextRow: List<Any> = currentIterator.next() // TODO here I can make better formatting
                    val message = String.format("%s\n\n\n/next", nextRow)
                    sendMessage.text = message

                    // TODO would like builder-like interface instead :(
                    val row = KeyboardRow()
                    row.add("Next month")
                    row.add("Next week")
                    // TODO Add
                    val rowArrayList = listOf(row)

                    val keyboard = ReplyKeyboardMarkup()
                            .setKeyboard(rowArrayList)
                            .setResizeKeyboard(true)
                            .setOneTimeKeyboard(true)
                    sendMessage.replyMarkup = keyboard
                }
            }


            try {
                execute(sendMessage) // Call method to send the message
            } catch (e: TelegramApiException) {
                logger.warn("Can't send a message", e)
            }
        }
    }

    private fun getBugHuntSheet(): List<List<Any>> {
        val response = spreadSheetService.spreadsheets().values()
                .get(bugHuntSheetId,
                        "A:AM")
                .execute()

        return response.getValues()
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

    val bugHuntBot = BugHuntBot(botToken, botOptions)

    botsApi.registerBot(bugHuntBot)
}