package press.lis.greeb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.telegram.telegrambots.bots.DefaultBotOptions;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import press.lis.greeb.processing.AnswerKeyboardAttacher;
import press.lis.greeb.processing.InlineQueryProcessor;

import java.util.HashSet;
import java.util.Set;

/**
 */
public class Bot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(Bot.class);
    private final String bot_token;

    private final Set<Long> subscribedUsers = new HashSet<>();

    private final InlineQueryProcessor inlineQueryProcessor;
    private final AnswerKeyboardAttacher answerKeyboardAttacher;

    public Bot(final String bot_token,
               final DefaultBotOptions options,
               final InlineQueryProcessor inlineQueryProcessor,
               final AnswerKeyboardAttacher answerKeyboardAttacher) {
        super(options);
        this.bot_token = bot_token;
        this.inlineQueryProcessor = inlineQueryProcessor;
        this.answerKeyboardAttacher = answerKeyboardAttacher;
    }

    public static void main(String[] args) {
        logger.info("Started");

        new BotConfiguration().createBot();
    }

    public void onUpdateReceived(final Update update) {
        logger.debug("Got update: {}", update);
        if (update.getCallbackQuery() != null) {
            CallbackQuery query = update.getCallbackQuery();
            AnswerCallbackQuery answer = inlineQueryProcessor.createReactionOnInlineAnswer(query);
            try {
                execute(answer);
            } catch (TelegramApiException e) {
                logger.error("Can't send a message!", e);
            }
        }

        if (update.hasMessage() && update.getMessage().hasText() &&
        update.getMessage().getText().equals("/subscribe")) {

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

            sendMessage = answerKeyboardAttacher.attachInlineKeyboard(sendMessage);

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
