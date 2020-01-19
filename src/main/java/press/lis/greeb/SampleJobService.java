package press.lis.greeb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

@Component
public class SampleJobService {

    private static final Logger logger = LoggerFactory.getLogger(SampleJobService.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    private Bot bot;


    @Scheduled(cron = "*/5 * * * * *")
    public void executeSampleJob() {
        String message = String.format("The time is now %s", dateFormat.format(new Date()));

        logger.info(message);

        bot.pingSubscribers(message);
    }
}