package press.lis.greeb.hammer_time

import com.typesafe.config.ConfigFactory
import mu.KotlinLogging
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.ApiContext
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import press.lis.greeb.SheetsClient

/**
 * @author Aleksandr Eliseev
 */
class HammerTimeMarathonBot(botToken: String, options: DefaultBotOptions?) : TelegramLongPollingBot(options) {
    private val botTokenInternal: String = botToken
    private val logger = KotlinLogging.logger {}

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
                "/subscribe" -> {
                    val userName = update.message.from.userName
                    val chatId = update.message.chatId

                    val values = getMarathonSheet()

                    val header = values[0]

                    val subscribed = values.subList(1, values.size - 1).filter {
                        it.size > 1 && it[1] == userName
                    }

                    val sendMessage: SendMessage = SendMessage() // Create a SendMessage object with mandatory fields
                            .setChatId(chatId)   // TODO здесь я хотел бы здесь правильный фильтр
                            .setText("Вы подписались! Я буду присылать ссылку на следующую неотмеченную главу каждый день." +
                                    "\nЕщё я могу отметить день прочитанным.")



                    TODO("ADD THE UPDATE OF TABLE HERE AND WARN IF NOT IN THE TABLE. ON SUCCESSFUL UPDATE MAKE link")
                }
                "/unsubscribe" -> {
                    TODO("ADD THE DELETION FROM TABLE HERE")
                }
                "done" -> {
                    val userName = update.message.from.userName
                    val chatId = update.message.chatId.toString()

                    val values = getMarathonSheet()

                    val header = values[0]

                    val subscribed = values.subList(1, values.size - 1).filter {
                        it.size > 2 && it[2] == chatId
                    }

                    subscribed.forEach {
                        val lastDay = it.indexOf("FALSE")

                        val sendMessage: SendMessage = if (lastDay == -1) {
                            SendMessage() // Create a SendMessage object with mandatory fields
                                    .setChatId(it[2].toString().toLong())   // TODO здесь я хотел бы здесь правильный фильтр
                                    .setText("Поздравляю! Вы уже закончили, а теперь пора отписываться.")

                            // TODO add unsubscription
                        } else {
                            val linkToMaterial = HammerTimeMarathonConstants.links[header[lastDay]]

                            SendMessage() // Create a SendMessage object with mandatory fields
                                    .setChatId(it[2].toString().toLong())
                                    .setText("${it[1]}, напоминаю про время молотков.\n" +
                                            "Ссылка на сегодняшнюю статью: $linkToMaterial")

                            // TODO mark that the day is finished
                        }

                        execute(sendMessage)
                    }

                    //x.spreadsheets().values().update(
                    //        s_id,
                    //        "AJ9",
                    //        ValueRange().setValues(listOf(listOf(true)))
                    //).execute()
                }
            }
        }
    }

    // TODO add the cron configuration for this method
    fun notifySubscribers() {
        val values = getMarathonSheet()

        val header = values[0]

        val subscribed = values.subList(1, values.size - 1).filter {
            it.size > 2 && it[2] != ""
        }

        subscribed.forEach {
            val lastDay = it.indexOf("FALSE")

            val sendMessage: SendMessage = if (lastDay == -1) {
                SendMessage() // Create a SendMessage object with mandatory fields
                        .setChatId(it[2].toString().toLong())
                        .setText("Поздравляю! Вы уже закончили, а теперь пора отписываться.")

                // TODO add unsubscription
            } else {
                val linkToMaterial = HammerTimeMarathonConstants.links[header[lastDay]]

                SendMessage() // Create a SendMessage object with mandatory fields
                        .setChatId(it[2].toString().toLong())
                        .setText("${it[1]}, напоминаю про время молотков.\n" +
                                "Ссылка на сегодняшнюю статью: $linkToMaterial")

                // TODO add keyboard here
            }

            execute(sendMessage)
        }


        // TODO Find the description of the last day, make a link to it
        // TODO add custom keyboard to complete day, show next days and unsubscribe
    }

    private fun getMarathonSheet(): List<MutableList<Any>> {
        val x = SheetsClient.getSheetService()

        val s_id = "1q3pPL_PMhnXbaAOsQ4EU96hAn7ZJuZQJguEEyvONipY"
        val response = x.spreadsheets().values()
                .get(s_id,
                        "A:AM")
                .execute()

        val values = response.getValues()
        return values
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