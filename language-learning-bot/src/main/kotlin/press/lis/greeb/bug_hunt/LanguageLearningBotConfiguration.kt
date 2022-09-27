package press.lis.greeb.bug_hunt

import com.typesafe.config.ConfigFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession

/**
 * @author Aleksandr Eliseev
 */

@Configuration
class BugHuntBotConfiguration {
    private val configFactory = ConfigFactory.load()

    @Bean
    fun createBot(): LanguageLearningBot {
        val botToken = configFactory.getString("bot.token")
        // Right now we're testing on a single production environment
        // val spreadsheetId = configFactory.getString("bot.spreadsheetId")
        val spreadsheetId = "1z7qBwbRTdQ0X3vqm0U1BEGO7PE1Z6oIwV0gz6BDjM-M"


        val botsApi = TelegramBotsApi(DefaultBotSession::class.java)

        val bot = LanguageLearningBot(
            botToken = botToken,
            spreadsheetId = spreadsheetId
        )

        try {
            botsApi.registerBot(bot)
        } catch (e: TelegramApiException) {
            e.printStackTrace()
        }
        return bot
    }

}

fun main() {
    val bugHuntBotConfiguration = BugHuntBotConfiguration()
    bugHuntBotConfiguration.createBot()
}