package press.lis.greeb.experiments.bot

import mu.KotlinLogging
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Update
import press.lis.greeb.spreadsheets.SheetsClient


/**
 * @author Aleksandr Eliseev
 */
class ExperimentalBot(botToken: String, options: DefaultBotOptions?) : TelegramLongPollingBot(options) {
    private val botTokenInternal: String = botToken
    private val logger = KotlinLogging.logger {}
    private val spreadSheetService = SheetsClient.sheetService
    private val experimentalSheetId = "14_EFQnHaewEcLL3aUkMktbdCbmOVXFJd-dGajBX6SWM"

    private lateinit var header: List<Any>

    private fun getBugHuntSheet(): Iterable<IndexedValue<List<Any>>> {
        val response = spreadSheetService.spreadsheets().values()
                .get(experimentalSheetId,
                        "A:AM")
                .execute()

        return response.getValues().withIndex()
    }


    private fun numberOfColumns(): Int {
        val bugHuntSheet = getBugHuntSheet()
        header = bugHuntSheet.first().value
        return header.size
    }


    override fun getBotUsername(): String {
        return "ExperimentalBot"
    }

    override fun getBotToken(): String {
        return botTokenInternal
    }

    override fun onUpdateReceived(update: Update?) {
        logger.info { "Got Update $update" }

        // Used only for me, ignore anyone else, to ensure nothing wrong happening
        if (update?.message?.text != null && update.message.from.userName == "eliseealex") {
            val sendMessage = SendMessage()
                    .setChatId(update.message.chatId)
                    .setText("Hello, world, there is ${numberOfColumns()} columns")

            execute(sendMessage)
        }
    }
}