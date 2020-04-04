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
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * @author Aleksandr Eliseev
 */
class BugHuntBot(botToken: String, options: DefaultBotOptions?) : TelegramLongPollingBot(options) {
    private val botTokenInternal: String = botToken
    private val logger = KotlinLogging.logger {}
    private val spreadSheetService = SheetsClient.sheetService
    private val bugHuntSheetId = "1u1pQx3RqqOFX-rr3Wuajyts_ufCeIQ21Mu0ndXCdv2M"

    private val currentIterator: Iterator<List<Any>> = getBugHuntSheet().iterator()
    private val header: List<Any> = currentIterator.next()


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
                "nw", "тц", "]", "ъ" -> sendMessage.text = "Scheduling to the next week" // TODO запилить разбор багов
                "nm", "ть", "[", "х" -> sendMessage.text = "Scheduling to the next month"
                "\\", "ё", "n", "/next", "Next bug" -> {
                    logger.debug("Got message: {}", update)
                    val nextRow: List<Any> = currentIterator.next() // TODO here I can make better formatting

                    val bug = nextRow.getOrNull(0)
                    val hardness = nextRow.getOrNull(1)
                    val field = nextRow.getOrNull(4)
                    val solution = nextRow.getOrNull(5)
                    val commentary = nextRow.getOrNull(6)
                    val nextTime = nextRow.getOrNull(7)

                    val message = """
                        *Bug:* $bug
                        
                        Should check since: $nextTime
                        Hardness: $hardness 
                        Field: $field
                        
                        Solution: $solution
                        
                        *Commentary:*
                        %s
                        
                        
                        
                        Go to the /next bug
                    """.trimIndent().format(commentary) // Formatting is needed for multiline comments

                    logger.info("Trying to send:\n$message")

                    sendMessage.text = message
                    sendMessage.enableMarkdown(true)

                    // TODO would like builder-like interface instead :(
                    val row1 = KeyboardRow()
                    row1.add("Next month")
                    row1.add("Next week")
                    val row2 = KeyboardRow()
                    row2.add("Next bug")

                    val rowArrayList = listOf(row1, row2)

                    val keyboard = ReplyKeyboardMarkup()
                            .setKeyboard(rowArrayList)
                            .setResizeKeyboard(true)
                            .setOneTimeKeyboard(true)
                    sendMessage.replyMarkup = keyboard
                }

                else -> {
                    sendMessage.text = """
                        Will add to the comment:
                        ${LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE)} -> $inputText
                    """.trimIndent()
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