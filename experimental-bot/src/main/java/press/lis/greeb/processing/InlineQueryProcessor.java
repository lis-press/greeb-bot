package press.lis.greeb.processing;

import org.slf4j.Logger;
import org.telegram.telegrambots.meta.api.methods.AnswerCallbackQuery;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;

import java.util.Map;

import static org.slf4j.LoggerFactory.getLogger;

public class InlineQueryProcessor {
    private final static Logger LOGGER = getLogger(InlineQueryProcessor.class);

    private final Map<String, String> answerMap;

    public InlineQueryProcessor(final Map<String, String> answerMap) {
        this.answerMap = answerMap;
    }

    public AnswerCallbackQuery createReactionOnInlineAnswer(final CallbackQuery update) {
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
