package press.lis.greeb;

import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;

import java.util.ArrayList;

public class AnswerKeyboardAttacher {

    private static ArrayList<ArrayList<String>> buttonTexts = new ArrayList<>(2);

    static {
        buttonTexts.add(new ArrayList<>(2));
        buttonTexts.add(new ArrayList<>(1));
        buttonTexts.get(0).add("Will go!");
        buttonTexts.get(0).add("Wouldn't go =(");
        buttonTexts.get(1).add("Dunno...");
    }

    public static SendMessage attachKeyboard(SendMessage message) {

        ArrayList<KeyboardRow> rowArrayList = new ArrayList<>(buttonTexts.size());
        for (ArrayList<String> buttonText : buttonTexts) {
            KeyboardRow row = new KeyboardRow();
            for (int j = 0; j < buttonText.size(); j++) {
                row.add(j, buttonText.get(j));
            }
            rowArrayList.add(row);
        }

        ReplyKeyboardMarkup keyboard = new ReplyKeyboardMarkup()
                .setKeyboard(rowArrayList)
                .setResizeKeyboard(true)
                .setOneTimeKeyboard(true);

        return message.setReplyMarkup(keyboard);

    }

}
