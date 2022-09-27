package press.lis.greeb.bug_hunt

import com.google.api.services.sheets.v4.model.ValueRange
import mu.KotlinLogging
import org.apache.commons.codec.binary.Base64
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto
import org.telegram.telegrambots.meta.api.objects.InputFile
import org.telegram.telegrambots.meta.api.objects.Update
import press.lis.greeb.spreadsheets.SheetsClient
import press.lis.greeb.spreadsheets.SpreadSheetsHelpers
import java.nio.ByteBuffer


/**
 * Note:
 * Bot should be Admin in any chat
 *
 * @author Aleksandr Eliseev
 */
class LanguageLearningBot(botToken: String, spreadsheetId: String) : TelegramLongPollingBot() {
    private val botTokenInternal: String = botToken
    private val logger = KotlinLogging.logger {}
    private val spreadSheetService = SheetsClient.sheetService
    private val spreadsheetIdInternal = spreadsheetId

    private lateinit var header: List<Any>
    private lateinit var chats: List<Any>
    private lateinit var rest: List<IndexedValue<List<Any>>>
    // Array lists are used at the execution time, but they are hidden

    private fun getBugHuntSheet(): Iterable<IndexedValue<List<Any>>> {
        val response = spreadSheetService.spreadsheets().values()
            .get(
                spreadsheetIdInternal,
                "A:ZZ"
            )
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

    override fun onUpdateReceived(update: Update?) {
        logger.info { "Got Update $update" }

        // List of admins are restricted here
        if (update?.message?.text != null &&
            (update.message.from.userName == "eliseealex" ||
                    update.message.from.userName == "fille_soleil" ||
                    update.message.from.userName == "du_rita")
        ) {

            val columnPattern = "([A-Z]+)".toRegex()
            val columnRowPattern = "([A-Z]+)([0-9]+)".toRegex()
            val rowPattern = "([0-9]+)".toRegex()

            initializeIndex()

            when {
                columnPattern.matches(update.message.text) -> {

                    val columnPatternMatch = columnPattern.find(update.message.text)!!

                    val spreadsheetColumnString = columnPatternMatch.groupValues[1]
                    val spreadsheetColumnIndex = SpreadSheetsHelpers.columnNameToNumber(spreadsheetColumnString)
                    val joinChatMessage = chats[spreadsheetColumnIndex].toString()
                    val channelId: Long = getChannelId(joinChatMessage)

                    val nextTaskText: String? = getNextTaskText(spreadsheetColumnIndex, spreadsheetColumnString)

                    logger.info { "Sending message to $channelId" }

                    if (nextTaskText != null) {
                        sendTask(nextTaskText, channelId)

                        // Присылаем информацию преподавателю
                        execute(
                            SendMessage(
                                update.message.chatId.toString(),
                                "Задание отправлено для: $joinChatMessage"
                            )
                        )
                    } else {
                        // Писылаем информацию преподавателю, что задания закончились и можно обратиться
                        //  к методисту, чтобы он выставил следующее задание
                        execute(
                            SendMessage(
                                channelId.toString(),
                                "Здорово! Ты прошёл все доступные задания!"
                            )
                        )

                        execute(
                            SendMessage(
                                update.message.chatId.toString(),
                                "Закончились доступные задания для: $joinChatMessage"
                            )
                        )
                    }
                }
                columnRowPattern.matches(update.message.text) -> {
                    val columnRowPatternMatch = columnRowPattern.find(update.message.text)!!

                    val spreadsheetColumnString = columnRowPatternMatch.groupValues[1]
                    val spreadsheetColumnIndex = SpreadSheetsHelpers.columnNameToNumber(spreadsheetColumnString)
                    val joinChatString = chats[spreadsheetColumnIndex].toString()
                    val channelId: Long = getChannelId(joinChatString)

                    val spreadsheetRowNumber = columnRowPatternMatch.groupValues[2].toInt()
                    // 1 for spreadsheet 1-starting arrays and 2 for chats and heading
                    val localRowNumber = spreadsheetRowNumber - 3

                    logger.info { "Sending message to $channelId" }

                    val messagesText = rest[localRowNumber].value[2].toString()

                    sendTask(messagesText, channelId)

                    execute(
                        SendMessage(
                            update.message.chatId.toString(),
                            "Задание отправлено для: $joinChatString"
                        )
                    )

                    spreadSheetService.spreadsheets().values().update(
                        spreadsheetIdInternal,
                        update.message.text,
                        ValueRange().setValues(listOf(listOf("Отправлено")))
                    ).setValueInputOption("USER_ENTERED").execute()
                }
                rowPattern.matches(update.message.text) -> {
                    val spreadsheetRowNumber = update.message.text.toInt()
                    // 1 for spreadsheet 1-starting arrays and 2 for chats and heading
                    val localRowNumber = spreadsheetRowNumber - 3

                    val messagesText = rest[localRowNumber].value[2].toString()

                    sendTask(messagesText, update.message.chatId)
                }
                else -> {
                    val sendMessage = SendMessage(
                        update.message.chatId.toString(),
                        "Не знаю такой команды, я могу отсылать сообщение человеку" +
                                "по его колонке в спредшите и конкретное задание!"
                    )

                    execute(sendMessage)
                }
            }
        } else if (update?.myChatMember != null) {
            val chat = update.myChatMember.chat


            val id = (chat.id * -1) - 1000000000000
            val title = chat.title

            initializeIndex()
            // TODO extract to the separate method
            val newColumnName = 'A' + header.size

            logger.info { "Adding a new column $newColumnName for chat id $id and title $title" }

            spreadSheetService.spreadsheets().values().update(
                spreadsheetIdInternal,
                "${newColumnName}1:${newColumnName}2",
                ValueRange().setValues(
                    listOf(
                        listOf(title),
                        listOf("https://web.telegram.org/#/im?p=c$id")
                        // TODO process ID correctly
                    )
                )
            ).setValueInputOption("USER_ENTERED").execute()

            initializeIndex()
        }
    }

    private fun sendTask(messagesText: String, chatId: Long) {
        messagesText.split("\n--new-message--\n").forEach { messageText ->
            if (messageText.contains(".jpg")) { // Need better regex based on usage pattern
                execute(
                    SendPhoto(
                        chatId.toString(),
                        InputFile(messageText)
                    )
                )
            } else {
                execute(
                    SendMessage(
                        chatId.toString(),
                        messageText
                    )
                )
            }
        }
    }

    /**
     * return nextTaskText if there is a next task or null if there is no next task
     */
    private fun getNextTaskText(
        spreadsheetColumnIndex: Int,
        spreadsheetColumnString: String
    ): String? {
        var nextTaskText: String? = null
        var lastDone: Int? = null

        for (row in rest) {
            if (row.value.size > spreadsheetColumnIndex &&
                row.value[spreadsheetColumnIndex] == "Следующее"
            ) {
                lastDone = row.index
                nextTaskText = row.value[2].toString()
                spreadSheetService.spreadsheets().values().update(
                    spreadsheetIdInternal,
                    "${spreadsheetColumnString}${row.index + 1}",
                    ValueRange().setValues(listOf(listOf("Отправлено")))
                ).setValueInputOption("USER_ENTERED").execute()
            }
            if (row.index == (lastDone ?: 0) + 1) {
                spreadSheetService.spreadsheets().values().update(
                    spreadsheetIdInternal,
                    "${spreadsheetColumnString}${row.index + 1}",
                    ValueRange().setValues(listOf(listOf("Следующее")))
                ).setValueInputOption("USER_ENTERED").execute()
            }
        }
        return nextTaskText
    }

    private fun getChannelId(joinChatMessage: String): Long {
        if (joinChatMessage.startsWith("https://t.me/joinchat/")) {

            val base64ChatId = joinChatMessage
                .replace("https://t.me/joinchat/", "")

            val messageAndChatByteArray = Base64.decodeBase64(base64ChatId)
            val messageAndChatByteBuffer = ByteBuffer.wrap(messageAndChatByteArray)
            val preChannelId = messageAndChatByteBuffer.getLong(0)

            return (1000000000000 + preChannelId) * -1
        } else {
            val channelIdString =
                joinChatMessage
                    .replace("https://web.telegram.org/#/im?p=c", "")
                    .split("_")[0]

            return (1000000000000 + channelIdString.toLong()) * -1
        }
    }
}