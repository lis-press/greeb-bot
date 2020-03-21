package press.lis.greeb.hammertime

import com.google.api.services.sheets.v4.model.ValueRange
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
import press.lis.greeb.spreadsheets.SheetsClient
import press.lis.greeb.spreadsheets.SpreadSheetsHelpers

/**
 * @author Aleksandr Eliseev
 */
class HammerTimeMarathonBot(botToken: String, options: DefaultBotOptions?) : TelegramLongPollingBot(options) {
    private val botTokenInternal: String = botToken
    private val logger = KotlinLogging.logger {}
    private val spreadSheetService = SheetsClient.sheetService
    private val hammertimeSheetId = "1q3pPL_PMhnXbaAOsQ4EU96hAn7ZJuZQJguEEyvONipY"

    override fun getBotUsername(): String {
        return "HammerTimeBot"
    }

    override fun getBotToken(): String {
        return botTokenInternal
    }

    override fun onUpdateReceived(update: Update?) {
        logger.info { "Got Update $update" }

        if (update?.message?.text != null) {
            when (update.message.text) {
                "/start" -> {
                    execute(SendMessage()
                            .setChatId(update.message.chatId)
                            .setText("Привет! Это бот для марафона HammerTime, нажмите /subscribe, чтобы " +
                                    "получать напоминания. Вы сможете отписаться в любой момент нажав /unsubscribe"))
                }
                "/subscribe" -> {
                    val userName = update.message.from.userName
                    val chatId = update.message.chatId

                    val values = getMarathonSheet()

                    values.subList(1, values.size - 1).forEachIndexed { index, daysRow ->
                        if (daysRow.size <= 2 ||
                                daysRow[1].toString().toLowerCase().substring(1) != userName.toLowerCase())
                            return@forEachIndexed

                        // TODO side effect in declaration seems bad
                        spreadSheetService.spreadsheets().values().update(
                                hammertimeSheetId,
                                "C${index + 2}",
                                ValueRange().setValues(listOf(listOf(chatId)))
                        ).setValueInputOption("USER_ENTERED").execute()

                        execute(SendMessage()
                                .setChatId(chatId)
                                .setText("Поздравляю! Вы подписаны, утром напомню о статье!")
                                .setReplyMarkup(replyKeyboardMarkup()))

                        return
                    }

                    execute(SendMessage()
                            .setChatId(chatId)
                            .setText("Не смог найти вас в таблице, проверьте ник в таблице и попробуйте ещё"))
                }
                "/unsubscribe" -> {

                    val userName = update.message.from.userName
                    val chatId = update.message.chatId

                    val values = getMarathonSheet()

                    values.subList(1, values.size - 1).forEachIndexed { index, daysRow ->
                        // TODO search by chat id
                        if (daysRow.size <= 2 ||
                                daysRow[1].toString().toLowerCase().substring(1) != userName.toLowerCase())
                            return@forEachIndexed

                        // TODO side effect in declaration seems bad
                        spreadSheetService.spreadsheets().values().update(
                                hammertimeSheetId,
                                "C${index + 2}",
                                ValueRange().setValues(listOf(listOf("")))
                        ).setValueInputOption("USER_ENTERED").execute()

                        execute(SendMessage()
                                .setChatId(chatId)
                                .setText("Вы отписаны! Если захотите вернуться, жмите на /subscribe"))

                        return
                    }

                    execute(SendMessage()
                            .setChatId(chatId)
                            .setText("Не смог найти вас в таблице, похоже, вы уже отписаны!"))
                }
                "Завершить текущий день" -> {
                    val chatId = update.message.chatId.toString()

                    val values = getMarathonSheet()

                    val header = values[0]

                    values.subList(1, values.size - 1).forEachIndexed { index, daysRow ->
                        if (daysRow.size <= 2 || daysRow[2] != chatId)
                            return@forEachIndexed

                        val lastDay = daysRow.indexOf("FALSE")

                        if (lastDay == -1) {
                            spreadSheetService.spreadsheets().values().update(
                                    hammertimeSheetId,
                                    "C${index + 2}",
                                    ValueRange().setValues(listOf(listOf("")))
                            ).setValueInputOption("USER_ENTERED").execute()

                            execute(SendMessage()
                                    .setChatId(daysRow[2].toString().toLong())   // TODO здесь я хотел бы здесь правильный фильтр
                                    .setText("Поздравляю! Вы уже закончили, теперь вы отписаны!"))
                        } else {
                            val columnNumberToA1Notation = SpreadSheetsHelpers.columnNumberToA1Notation(lastDay)
                            val updateCellA1Notation = "$columnNumberToA1Notation${index + 2}"

                            spreadSheetService.spreadsheets().values().update(
                                    hammertimeSheetId,
                                    updateCellA1Notation,
                                    ValueRange().setValues(listOf(listOf(true)))
                            ).setValueInputOption("USER_ENTERED").execute()

                            execute(SendMessage() // Create a SendMessage object with mandatory fields
                                    .setChatId(daysRow[2].toString().toLong())
                                    .setText("${daysRow[1]}, отметил день как выполненный.\n" +
                                            "До завтра")
                                    .setReplyMarkup(replyKeyboardMarkup()))
                        }
                    }
                }
                "Следующий день" -> {
                    val chatId = update.message.chatId.toString()

                    val values = getMarathonSheet()

                    val header = values[0]

                    values.subList(1, values.size - 1).forEachIndexed { index, daysRow ->
                        if (daysRow.size <= 2 || daysRow[2] != chatId) // TODO to extract i should make this check different
                            return@forEachIndexed

                        val lastDay = daysRow.indexOf("FALSE")

                        if (lastDay == -1) {
                            spreadSheetService.spreadsheets().values().update(
                                    hammertimeSheetId,
                                    "C${index + 2}",
                                    ValueRange().setValues(listOf(listOf("")))
                            ).setValueInputOption("USER_ENTERED").execute()

                            execute(SendMessage()
                                    .setChatId(daysRow[2].toString().toLong())
                                    .setText("Поздравляю! Вы уже закончили, теперь вы отписаны!"))
                        } else {
                            val linkToMaterial = HammerTimeMarathonConstants.links[header[lastDay]]

                            execute(SendMessage()
                                    .setChatId(daysRow[2].toString().toLong())
                                    .setText("${daysRow[1]}, напоминаю про время молотков.\n" +
                                            "Ссылка на сегодняшнюю статью: $linkToMaterial")
                                    .setReplyMarkup(replyKeyboardMarkup()))

                        }
                    }
                }
            }
        }
    }

    // TODO add the cron configuration for this method
    fun notifySubscribers() {
        val values = getMarathonSheet()

        val header = values[0]

        // TODO seems like this could be extracted to the separate method
        values.subList(1, values.size - 1).forEachIndexed { index, daysRow ->
            if (daysRow.size <= 2 || daysRow[2] == "") // TODO to extract i should make this check different
                return@forEachIndexed

            val lastDay = daysRow.indexOf("FALSE")

            if (lastDay == -1) {
                spreadSheetService.spreadsheets().values().update(
                        hammertimeSheetId,
                        "C${index + 2}",
                        ValueRange().setValues(listOf(listOf("")))
                ).setValueInputOption("USER_ENTERED").execute()

                execute(SendMessage()
                        .setChatId(daysRow[2].toString().toLong())
                        .setText("Поздравляю! Вы уже закончили, теперь вы отписаны!"))
            } else {
                val linkToMaterial = HammerTimeMarathonConstants.links[header[lastDay]]

                execute(SendMessage()
                        .setChatId(daysRow[2].toString().toLong())
                        .setText("${daysRow[1]}, напоминаю про время молотков.\n" +
                                "Ссылка на сегодняшнюю статью: $linkToMaterial")
                        .setReplyMarkup(replyKeyboardMarkup()))

            }
        }
    }

    private fun replyKeyboardMarkup(): ReplyKeyboardMarkup {
        val row1 = KeyboardRow()
        row1.add("/unsubscribe")
        val row2 = KeyboardRow()
        row2.add("Следующий день")
        val row3 = KeyboardRow()
        row2.add("Завершить текущий день")

        val rowArrayList = listOf(row1, row2, row3)

        return ReplyKeyboardMarkup()
                .setKeyboard(rowArrayList)
                .setResizeKeyboard(true)
                .setOneTimeKeyboard(true)
    }

    private fun getMarathonSheet(): List<MutableList<Any>> {
        val response = spreadSheetService.spreadsheets().values()
                .get(hammertimeSheetId,
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

    val hammerTimeBot = HammerTimeMarathonBot(botToken, botOptions)

    hammerTimeBot.notifySubscribers()

    botsApi.registerBot(hammerTimeBot)
}