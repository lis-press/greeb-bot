package press.lis.greeb.processing;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;
import java.util.List;

public class AnswerKeyboardAttacher {

    private final List<List<String>> buttonTexts;

    public AnswerKeyboardAttacher(final List<List<String>> buttonTexts) {
        this.buttonTexts = buttonTexts;
    }

    public SendMessage attachKeyboard(final SendMessage message) {

        List<KeyboardRow> rowList = new ArrayList<>(buttonTexts.size());
        for (List<String> buttonText : buttonTexts) {
            KeyboardRow row = new KeyboardRow();
            for (String text : buttonText) {
                row.add(text);
            }
            rowList.add(row);
        }

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup()
                .setKeyboard(rowList)
                .setResizeKeyboard(true)
                .setOneTimeKeyboard(true);

        return message.setReplyMarkup(keyboard);

    }

    public SendMessage attachInlineKeyboard(final SendMessage message) {

        final List<List<InlineKeyboardButton>> buttonsArray = new ArrayList<>(buttonTexts.size());
        for (List<String> buttonText : buttonTexts) {
            List<InlineKeyboardButton> row = new ArrayList<>(buttonText.size());
            for (String text : buttonText) {
                InlineKeyboardButton button = new InlineKeyboardButton();
                button.setText(text);
                button.setCallbackData(text);
                row.add(button);
            }
            buttonsArray.add(row);
        }

        InlineKeyboardMarkup keyboard = new InlineKeyboardMarkup();
        keyboard.setKeyboard(buttonsArray);

        return message.setReplyMarkup(keyboard);
    }


}
