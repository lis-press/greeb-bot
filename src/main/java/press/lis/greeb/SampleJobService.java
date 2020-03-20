package press.lis.greeb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import press.lis.greeb.hammer_time.HammerTimeMarathonBot;

import java.text.SimpleDateFormat;

@Component
public class SampleJobService {

    private static final Logger logger = LoggerFactory.getLogger(SampleJobService.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    @Autowired
    private HammerTimeMarathonBot bot;


    @Scheduled(cron = "*/5 * * * * *")
    public void executeSampleJob() {
        logger.info("Notifying subscribers");

        bot.notifySubscribers();
    }
}