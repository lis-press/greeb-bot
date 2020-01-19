package press.lis.greeb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Aleksandr Eliseev
 */
public class Bot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);
    private final String bot_token;

    private final Set<Long> subscribedUsers = new HashSet<>();

    public Bot(final String bot_token, final DefaultBotOptions options) {
        super(options);
        this.bot_token = bot_token;
    }

    public static void main(String[] args) {
        logger.info("Started");

        new BotConfiguration().createBot();
    }

    public void onUpdateReceived(final Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            logger.debug("Got message: {}", update);

            final Long userChatId = update.getMessage().getChatId();
            subscribedUsers.add(userChatId);

            SendMessage message = new SendMessage() // Create a SendMessage object with mandatory fields
                    .setChatId(userChatId)
                    .setText("You've been subscribed to dates :)");
            try {
                execute(message); // Call method to send the message
            } catch (TelegramApiException e) {
                logger.warn("Can't send a message", e);
            }
        }
    }

    public void pingSubscribers(final String message) {
        for (final Long subscribedUser : subscribedUsers) {
            SendMessage sendMessage = new SendMessage() // Create a SendMessage object with mandatory fields
                    .setChatId(subscribedUser)
                    .setText(message);

            try {
                execute(sendMessage); // Call method to send the message
            } catch (TelegramApiException e) {
                logger.warn("Can't send a message", e);
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
