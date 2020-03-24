package press.lis.greeb.hammertime;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    public static final Logger LOGGER = LoggerFactory.getLogger(BotConfiguration.class);

    private static String PROXY_HOST = "localhost";
    private static Integer PROXY_PORT = 1337;

    @Bean
    public HammerTimeMarathonBot createBot() {
        final Config configFactory = ConfigFactory.load();
        final String bot_token = configFactory.getString("bot.token");
        ApiContextInitializer.init();


        final TelegramBotsApi botsApi = new TelegramBotsApi();

        final DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);


        if (!configFactory.hasPath("bot.no_proxy") || !configFactory.getBoolean("bot.no_proxy")) {
            LOGGER.info("Setting up proxy");

            botOptions.setProxyHost(PROXY_HOST);
            botOptions.setProxyPort(PROXY_PORT);
            // Select proxy type: [HTTP|SOCKS4|SOCKS5] (default: NO_PROXY)
            botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);
        } else {
            LOGGER.info("No proxy configured");
        }


        final HammerTimeMarathonBot bot = new HammerTimeMarathonBot(bot_token, botOptions);

        try {
            botsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return bot;
    }
}
