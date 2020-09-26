package press.lis.greeb.bug_hunt

import com.typesafe.config.ConfigFactory
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.ApiContextInitializer
import org.telegram.telegrambots.bots.DefaultBotOptions
import org.telegram.telegrambots.meta.ApiContext
import org.telegram.telegrambots.meta.TelegramBotsApi
import org.telegram.telegrambots.meta.exceptions.TelegramApiException

/**
 * @author Aleksandr Eliseev
 */

@Configuration
class BugHuntBotConfiguration {
    private val configFactory = ConfigFactory.load()

    @Bean
    fun createBot(): LanguageLearningBot {
        val botToken = configFactory.getString("bot.token")

        ApiContextInitializer.init()

        val botsApi = TelegramBotsApi()
        val botOptions = ApiContext.getInstance(DefaultBotOptions::class.java)

        val bot = LanguageLearningBot(
                botToken = botToken,
                options = botOptions)

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