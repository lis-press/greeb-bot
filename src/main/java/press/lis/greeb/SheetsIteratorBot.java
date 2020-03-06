package press.lis.greeb;

import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ValueRange;
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
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * @author Aleksandr Eliseev
 */
public class SheetsIteratorBot extends TelegramLongPollingBot {
    private static final Logger logger = LoggerFactory.getLogger(SheetsIteratorBot.class);
    private final String bot_token;
    private final List<List<Object>> currentSheet;
    private final Iterator<List<Object>> currentIterator;

    public SheetsIteratorBot(final String bot_token, final DefaultBotOptions options) throws IOException, GeneralSecurityException {
        super(options);
        this.bot_token = bot_token;
        this.currentSheet = readSheet();
        this.currentIterator = currentSheet.iterator();
        currentIterator.next();
    }

    public static void main(String[] args) throws Exception {
        logger.info("Started");

        final String bot_token = ConfigFactory.load().getString("bot.token");
        ApiContextInitializer.init();

        final TelegramBotsApi botsApi = new TelegramBotsApi();

        final DefaultBotOptions botOptions = ApiContext.getInstance(DefaultBotOptions.class);

        botOptions.setProxyHost("localhost");
        botOptions.setProxyPort(1337);
        // Select proxy type: [HTTP|SOCKS4|SOCKS5] (default: NO_PROXY)
        botOptions.setProxyType(DefaultBotOptions.ProxyType.SOCKS5);

        final SheetsIteratorBot sheetsIteratorBot = new SheetsIteratorBot(bot_token, botOptions);

        try {
            botsApi.registerBot(sheetsIteratorBot);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private List<List<Object>> readSheet() throws IOException, GeneralSecurityException {
        final Sheets sheetService = SheetsClient.getSheetService();

        final String spreadsheetId = "1u1pQx3RqqOFX-rr3Wuajyts_ufCeIQ21Mu0ndXCdv2M";
        final String range = "A:Z";
        ValueRange response = sheetService.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();

        return response.getValues();

    }

    public void onUpdateReceived(final Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            final String inputText = update.getMessage().getText();

            final Long userChatId = update.getMessage().getChatId();

            final SendMessage sendMessage = new SendMessage() // Create a SendMessage object with mandatory fields
                    .setChatId(userChatId);

            switch (inputText) {
                case "nw":
                case "тц":
                    sendMessage.setText("Scheduling to the next week");
                    break;

                case "nm":
                case "ть":
                    sendMessage.setText("Scheduling to the next month");
                    break;

                default:
                    logger.debug("Got message: {}", update);

                    final List<Object> nextRow = currentIterator.next();
                    final String message = String.format("%s\n\n\n/next", nextRow);

                    sendMessage.setText(message);

                    // TODO would like builder-like interface instead :(
                    KeyboardRow row = new KeyboardRow();
                    row.add("next");
                    row.add("ok");

                    List<KeyboardRow> rowArrayList = Collections.singletonList(row);

                    ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup()
                            .setKeyboard(rowArrayList)
                            .setResizeKeyboard(true)
                            .setOneTimeKeyboard(true);

                    sendMessage.setReplyMarkup(keyboard);
                    break;
            }


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
