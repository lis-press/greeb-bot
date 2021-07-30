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

        val botsApi = TelegramBotsApi(DefaultBotSession::class.java)

        val bot = LanguageLearningBot(
            botToken = botToken
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