package press.lis.greeb;

import com.typesafe.config.ConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import press.lis.greeb.processing.AnswerKeyboardAttacher;
import press.lis.greeb.processing.InlineQueryProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Aleksandr Eliseev
 */
@Configuration
public class BotConfiguration {
    private static String PROXY_HOST = "localhost";
    private static Integer PROXY_PORT = 1337;
    private static ArrayList<ArrayList<String>> buttonTexts = new ArrayList<>();
    private static Map<String, String> reactionsMap = new HashMap<>();

    static {
        buttonTexts.add(new ArrayList<>(2));
        buttonTexts.add(new ArrayList<>(1));
        buttonTexts.get(0).add("Really good!");
        buttonTexts.get(0).add("Not as good as I want =(");
        buttonTexts.get(1).add("Average...");
        reactionsMap.put(buttonTexts.get(0).get(0), "Cool!");
        reactionsMap.put(buttonTexts.get(0).get(1), "Disappointing =(");
        reactionsMap.put(buttonTexts.get(1).get(0), "Okay...");
    }

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

        final AnswerKeyboardAttacher attacher = new AnswerKeyboardAttacher(buttonTexts);

        final InlineQueryProcessor processor = new InlineQueryProcessor(reactionsMap);

        final Bot bot = new Bot(bot_token, botOptions, processor, attacher);

        try {
            botsApi.registerBot(bot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }

        return bot;
    }
}
