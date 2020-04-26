package press.lis.greeb.bug_hunt

import com.google.api.services.sheets.v4.model.ValueRange
import mu.KotlinLogging
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import press.lis.greeb.spreadsheets.SheetsClient
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


/**
 * @author Aleksandr Eliseev
 */
class BugHuntBot(botToken: String, chatId: Long, options: DefaultBotOptions?) : TelegramLongPollingBot(options) {
    private val botTokenInternal: String = botToken
    private val chatIdInternal: Long = chatId
    private val logger = KotlinLogging.logger {}
    private val spreadSheetService = SheetsClient.sheetService
    private val bugHuntSheetId = "1u1pQx3RqqOFX-rr3Wuajyts_ufCeIQ21Mu0ndXCdv2M"

    private val dateTimeFormatter = DateTimeFormatter.ISO_LOCAL_DATE

    private lateinit var bugHuntIterator: Iterator<IndexedValue<List<Any>>>
    private lateinit var header: List<Any>
    private lateinit var currentRowIndexed: IndexedValue<List<Any>>

    private fun getBugHuntSheet(): Iterable<IndexedValue<List<Any>>> {
        val response = spreadSheetService.spreadsheets().values()
                .get(bugHuntSheetId,
                        "A:AM")
                .execute()

        return response.getValues().withIndex()
    }

    init {
        // TODO made to initialize currentRowIndexed correctly and make deployment procedure easier
        // TODO not sure should be included in final interface
        sendStatistics()
    }


    private fun initializeBugHuntSheetsDefault() {
        val bugHuntSheet = getBugHuntSheet()
        header = bugHuntSheet.first().value

        val usedBugHuntSheet = bugHuntSheet.drop(1)
        bugHuntIterator = usedBugHuntSheet.iterator()
    }


    private fun initializeBugHuntSheetsWithPriority() {
        val bugHuntSheet = getBugHuntSheet()
        header = bugHuntSheet.first().value

        // TODO looks like strategy will be better here
        val usedBugHuntSheet =
                bugHuntSheet
                        .drop(1)
                        .sortedWith(compareBy(
                                { it.value[3].toString().toLong() < 100 },
                                // Nulls should be last in this priority
                                // TODO desc will be better here?
                                {
                                    LocalDate.parse(it.value.getOrElse(7) { "2000-01-01" }.toString(),
                                            dateTimeFormatter)
                                }))
                        .reversed()

        bugHuntIterator = usedBugHuntSheet.iterator()
    }


    override fun getBotUsername(): String {
        return "BugHuntBot"
    }

    override fun getBotToken(): String {
        return botTokenInternal
    }

    private fun updateNextCheckDate(dateString: String) {
        spreadSheetService.spreadsheets().values().update(
                bugHuntSheetId,
                "H${currentRowIndexed.index + 1}",
                ValueRange().setValues(listOf(listOf(dateString)))
        ).setValueInputOption("USER_ENTERED").execute()
    }

    override fun onUpdateReceived(update: Update?) {
        logger.info { "Got Update $update" }

        // Used only for me, ignore anyone else, to ensure nothing wrong happening
        if (update?.message?.text != null && update.message.from.userName == "eliseealex") {
            val inputText = update.message.text

            val userChatId = update.message.chatId

            val sendMessage = SendMessage()
                    .setChatId(userChatId)

            val nowDate = LocalDateTime.now()
            when (inputText) {
                // TODO we can refactor so that, Keyboard will be consistent with the processing and generate hotkeys
                "nw", "тц", "]", "ъ", "Next week" -> {
                    sendMessage.text = "Scheduled to the next week"
                    updateNextCheckDate(nowDate.plusDays(7).format(dateTimeFormatter))
                }
                "]]", "ъъ" -> {
                    sendMessage.text = "Scheduled in two week"
                    updateNextCheckDate(nowDate.plusDays(14).format(dateTimeFormatter))
                }
                "]]]", "ъъъ" -> {
                    sendMessage.text = "Scheduled in three weeks"
                    updateNextCheckDate(nowDate.plusDays(21).format(dateTimeFormatter))
                }
                "nm", "ть", "[", "х", "Next month" -> {
                    sendMessage.text = "Scheduling to the next month"
                    updateNextCheckDate(nowDate.plusMonths(1).format(dateTimeFormatter))
                }
                "ntm", "теь", "p", "з", "In 2 months" -> {
                    sendMessage.text = "Scheduling to the month after the next"
                    updateNextCheckDate(nowDate.plusMonths(2).format(dateTimeFormatter))
                }
                // TODO возможно, стоит вынести на клавиатуру
                "c", "с", "clear" -> {
                    initializeBugHuntSheetsDefault()

                    val message = getNextMessage()
                    sendMessage.text = "*Cleared*\n\n$message"
                    sendMessage.enableMarkdown(true)
                }
                "cc", "сс", "clear with priority" -> {
                    initializeBugHuntSheetsWithPriority()

                    val message = getNextMessage()
                    sendMessage.text = "*Cleared with priority*\n\n$message"
                    sendMessage.enableMarkdown(true)
                }
                "\\", "ё", "n", "/next", "Next bug" -> {
                    val message = getNextMessage()

                    sendMessage.text = message
                    sendMessage.enableMarkdown(true)
                }

                else -> {
                    val commentToAdd = "${nowDate.format(dateTimeFormatter)} -> $inputText"
                    sendMessage.text = """
                        Will add to the comment (will rewrite the last try):
                        $commentToAdd
                    """.trimIndent()

                    val comment = currentRowIndexed.value.getOrElse(6) { "" }.toString()

                    val generatedPrefix = "--g--"
                    val genStart = comment.indexOf(generatedPrefix)

                    // Write test?
                    val nextComment = if (genStart == -1) {
                        "$comment\n$generatedPrefix\n$commentToAdd"
                    } else {
                        val preNewComment = comment.substring(0, genStart + generatedPrefix.length)
                        val postNewComment = comment.substring(genStart + generatedPrefix.length)
                        "$preNewComment\n$commentToAdd\n$postNewComment"
                    }

                    spreadSheetService.spreadsheets().values().update(
                            bugHuntSheetId,
                            "G${currentRowIndexed.index + 1}",
                            ValueRange().setValues(listOf(listOf(nextComment)))
                    ).setValueInputOption("USER_ENTERED").execute()
                }
            }

            val row1 = KeyboardRow()
            row1.add("In 2 months")
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

            try {
                execute(sendMessage) // Call method to send the message
            } catch (e: TelegramApiException) {
                logger.warn("Can't send a message", e)
            }
        }
    }

    private fun getNextMessage(): String {
        currentRowIndexed = bugHuntIterator.next()
        val currentRow: List<Any> = currentRowIndexed.value

        // TODO use better abstraction for parsing of the string
        val bug = currentRow.getOrNull(0)
        val hardness = currentRow.getOrNull(1)
        val field = currentRow.getOrNull(4)
        val solution = currentRow.getOrNull(5)
        val comment = currentRow.getOrNull(6)
        val nextTime = currentRow.getOrNull(7)

        val message = """
                            *Bug:* $bug
                            
                            Should check since: $nextTime
                            Hardness: $hardness 
                            Field: $field
                            
                            Solution: $solution
                            
                            *Commentary:*
                            %s
                            
                            
                            
                            Go to the /next bug
                        """.trimIndent().format(comment) // Formatting is needed for multiline comments

        logger.info("Trying to send:\n$message")
        return message
    }

    fun sendStatistics() {
        val bugHuntSheet = getBugHuntSheet()
        logger.info("Started to send statistics")

        val nowDateString = LocalDateTime.now().format(dateTimeFormatter)

        val bugsString = bugHuntSheet
                .filter { it.value.getOrNull(7) == nowDateString }
                .joinToString(separator = "\n\n") { "${it.value.getOrNull(1)}: ${it.value.getOrNull(0)}" }

        if (bugsString != "") {
            val sendMessage = SendMessage()
                    .setChatId(chatIdInternal)
                    .setText("Bugs that should be checked today:\n\n$bugsString")

            execute(sendMessage)
        }

        initializeBugHuntSheetsWithPriority()

        val message = getNextMessage()

        execute(SendMessage()
                .setChatId(chatIdInternal)
                .setText("*First message in new priority*\n\n$message")
                .enableMarkdown(true))
    }
}