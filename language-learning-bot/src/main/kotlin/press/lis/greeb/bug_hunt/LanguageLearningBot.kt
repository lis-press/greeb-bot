package press.lis.greeb.bug_hunt

import com.google.api.services.sheets.v4.model.ValueRange
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
class LanguageLearningBot(botToken: String, options: DefaultBotOptions?) : TelegramLongPollingBot(options) {
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
    override fun onUpdateReceived(update: Update?) {
        logger.info { "Got Update $update" }

        // Used only for me, ignore anyone else, to ensure nothing wrong happening
        if (update?.message?.text != null && update.message.from.userName == "eliseealex") {

            val columnPattern = "[A-Z]".toRegex() // TODO two letters case
            val columnRowPattern = "[A-Z]([0-9]+)".toRegex()  // TODO could make a single regex
            val rowPattern = "([0-9]+)".toRegex()

            when {
                columnPattern.matches(update.message.text) -> {

                    initializeIndex()

                    var lastDone: Int? = null
                    var nextTaskText: String? = null

                    for (row in rest) {
                        if (row.value.size > update.message.text[0] - 'A' &&
                                row.value[update.message.text[0] - 'A'] == "Следующее") {
                            lastDone = row.index
                            nextTaskText = row.value[2].toString()
                            spreadSheetService.spreadsheets().values().update(
                                    experimentalSheetId,
                                    "${update.message.text}${row.index + 1}",
                                    ValueRange().setValues(listOf(listOf("Отправлено")))
                            ).setValueInputOption("USER_ENTERED").execute()
                        }
                        if (row.index == (lastDone ?: 0) + 1) {
                            // TODO останавливаться, если нет задания или закончился курс?
                            // TODO я бы этот момент обсудил и разобрался, в какой момент останавливаться?

                            spreadSheetService.spreadsheets().values().update(
                                    experimentalSheetId,
                                    "${update.message.text}${row.index + 1}",
                                    ValueRange().setValues(listOf(listOf("Следующее")))
                            ).setValueInputOption("USER_ENTERED").execute()
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

                        execute(SendMessage()
                                .setChatId(update.message.chatId)
                                .setText("Задание отправлено для: $joinChatMessage"))
                    } else {
                        // TODO прислать информацию преподавателю, что задания закончились и можно обратиться
                        //  к методисту, чтобы он выставил следующее задание
                        execute(SendMessage()
                                .setChatId(channelId)
                                .setText("Здорово! Ты прошёл все доступные задания!"))

                        execute(SendMessage()
                                .setChatId(update.message.chatId)
                                .setText("Закончились доступные задания для: $joinChatMessage"))
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

                    execute(SendMessage()
                            .setChatId(update.message.chatId)
                            .setText("Задание отправлено для: $joinChatMessage"))

                    spreadSheetService.spreadsheets().values().update(
                            experimentalSheetId,
                            update.message.text,
                            ValueRange().setValues(listOf(listOf("Отправлено")))
                    ).setValueInputOption("USER_ENTERED").execute()
                }
                rowPattern.matches(update.message.text) -> {
                    val spreadsheetRowNumber = update.message.text.toInt()
                    // 1 for spreadsheet 1-starting arrays and 2 for chats and heading
                    val localRowNumber = spreadsheetRowNumber - 3

                    initializeIndex()

                    val messagesText = rest[localRowNumber].value[2].toString()

                    messagesText.split("\n--new-message--\n").forEach { messageText ->
                        if (messageText.contains(".jpg")) { // TODO just to test, need better regex
                            execute(SendPhoto()
                                    .setChatId(update.message.chatId)
                                    .setPhoto(messageText))
                        } else {
                            execute(SendMessage()
                                    .setChatId(update.message.chatId)
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

            /*
            * TODO мигрировать это задание в какую-нибудь более понятную документацию
            *
            * Как работать с ботом?
            * Бот сделан для работы с базой знаний, которая находится в Spreadsheet:
            * В ней есть задания и список учеников.
            * Чтобы добавить нового ученика, нужно в новую колонку добавить нового ученика
            * Напротив первого задания для уровня ученика нужно поставить "Следующее",
            * Напротив последнего задания курса, нужно поставить "Последнее"
            *
            * Когда вы добавили ученику задание, чтобы бот прислал задание, нужно прислать боту колонку с учеником
            * В таком случае бот выдаст следующее задание:
            * Например нажмите `D` и отправьте сообщение
            *
            * Кроме того, бот может выдать факультативное задание (конкретный номер задания конкретному ученику)
            * Чтобы выдать ученику D задание на строчке 140 отправьте боту `D140`
            * При этом, если вы отправите следующее задание боту, он отправит следующее задание по программе
            *
            * Когда ученик сделал задание, он должен позвать учителя, чтобы тот проверил, дал обратную связь
            * и выдал задание. Учитель даёт обратную связь и выдаёт следующее
            * задание ученикам не нужно работать с ботом, они видят только то, что он им присылает задание в чат,
            * в канал, в которому ученики общаются с преподавателем.
            *
            * TODO реализовать этот момент
            * Бот отвечает только на сообщения Администраторов
            * Список администраторов перечислен на отдельной странице.
            *
            *
            * Работа с базой знаний
            * В базе знаний важна последовательность заданий бот всегда назначает следующее задание по порядку
            *
            * TODO возможности для автоматизации
            *  автоматическое создание группы при добавлении нового ученика
            *  автоматическое добавление администраторов в группы
            *  двухбуквенные ученики
            *  инлайн видео?
            *  можно отслеживать оплаты по тому, какое последнее задание есть у ученика?
            *  можно добавить снижение количества провереных заданий по ходу?
            *
            * */
        }
    }
}