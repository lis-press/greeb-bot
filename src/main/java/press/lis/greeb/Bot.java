package press.lis.greeb;

import com.typesafe.config.ConfigFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.ApiContext;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

/**
 * @author Aleksandr Eliseev
 */
public class Bot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);

    private static String PROXY_HOST = "localhost" /* proxy host */;
    private static Integer PROXY_PORT = 1337 /* proxy port */;
    private final String bot_token;

    public Bot(final String bot_token, final DefaultBotOptions options) {
        super(options);
        this.bot_token = bot_token;
    }

    public static void main(String[] args) {
        logger.info("Started");

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

    }

    public void onUpdateReceived(final Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            logger.debug("Got message: {}", update);

            final String timer_link = "https://www.google.com/search?q=90+minutes+timer&oq=90+minutes+timer";
            SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
                    .setChatId(update.getMessage().getChatId())
                    .setText(String.format("Ok, got it, that's your link:\n%s",timer_link));
            try {
                execute(message); // Call method to send the message
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
    }

    public String getBotUsername() {
        return "Greeb";
    }

    public String getBotToken() {
        return bot_token;
    }
}
