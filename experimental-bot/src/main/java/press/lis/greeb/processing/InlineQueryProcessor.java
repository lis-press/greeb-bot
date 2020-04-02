package press.lis.greeb.processing;

import org.slf4j.Logger;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class InlineQueryProcessor {
    private static Logger logger = getLogger(InlineQueryProcessor.class);

    private Map<String, String> answerMap;

    public InlineQueryProcessor(Map answerMap) {
        this.answerMap = answerMap;
    }

    public AnswerCallbackQuery createReactionOnInlineAnswer(CallbackQuery update) {
        AnswerCallbackQuery answer = new AnswerCallbackQuery();
        answer.setCallbackQueryId(update.getId());
        String text = answerMap.get(update.getData());
        if (text != null) {
            answer.setText(answerMap.get(update.getData()));
        } else {
            return null;
        }
        return answer;
    }


}
