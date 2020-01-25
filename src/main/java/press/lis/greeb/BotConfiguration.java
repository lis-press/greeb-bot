package press.lis.greeb;

import com.typesafe.config.ConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * @author Aleksandr Eliseev
 */
@Configuration
public class BotConfiguration {
    private static String PROXY_HOST = "localhost" /* proxy host */;
    private static Integer PROXY_PORT = 1337 /* proxy port */;

    @Bean
    public Bot createBot() {
        final String bot_token = ConfigFactory.load().getString("bot.token");
        ApiContextInitializer.init();

        final TelegramBotsApi botsApi = new TelegramBotsApi();

        final DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

        botOptions.setProxyHost(PROXY_HOST);
        botOptions.setProxyPort(PROXY_PORT);
        // Select proxy type: [HTTP|SOCKS4|SOCKS5] (default: NO_PROXY)
        botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);

        final Bot bot = new Bot(bot_token, botOptions);

        try {
            botsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return bot;
    }
}
