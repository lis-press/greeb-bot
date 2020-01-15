package press.lis.greeb;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;

public class AnswerKeyboardAttacher {

    public static SendMessage attachKeyboard(SendMessage message) {

        KeyboardRow row1 = new KeyboardRow();
        row1.add(0, "Will go!");
        row1.add(1, "Wouldn't go =(");

        KeyboardRow row2 = new KeyboardRow();
        row2.add(0, "Dunno...");

        ArrayList<KeyboardRow> rowArrayList = new ArrayList<>();
        rowArrayList.add(row1);
        rowArrayList.add(row2);

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup()
                .setKeyboard(rowArrayList)
                .setResizeKeyboard(true)
                .setOneTimeKeyboard(true);

        return message.setReplyMarkup(keyboard);

    }
}
