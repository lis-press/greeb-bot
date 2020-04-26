package press.lis.greeb.bug_hunt

import com.typesafe.config.ConfigFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
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
    private val logger: Logger = LoggerFactory.getLogger(BugHuntBotConfiguration::class.java)

    private val proxyHost = "localhost"
    private val proxyPort = 1337

    @Bean
    fun createBot(): BugHuntBot {
        val configFactory = ConfigFactory.load()
        val botToken = configFactory.getString("bot.token")
        val chatId = configFactory.getLong("bot.chatId")

        ApiContextInitializer.init()

        val botsApi = TelegramBotsApi()
        val botOptions = ApiContext.getInstance(DefaultBotOptions::class.java)

        if (!configFactory.hasPath("bot.no_proxy") || !configFactory.getBoolean("bot.no_proxy")) {
            logger.info("Setting up proxy")
            botOptions.proxyHost = proxyHost
            botOptions.proxyPort = proxyPort
            // Select proxy type: [HTTP|SOCKS4|SOCKS5] (default: NO_PROXY)
            botOptions.proxyType = DefaultBotOptions.ProxyType.SOCKS5
        } else {
            logger.info("No proxy configured")
        }

        val bot = BugHuntBot(
                botToken = botToken,
                chatId = chatId,
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
    BugHuntBotConfiguration().createBot()
}