package press.lis.greeb.hammertime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class SchedulingService {

    private static final Logger logger = LoggerFactory.getLogger(SchedulingService.class);

    @Autowired
    private HammerTimeMarathonBot bot;


    @Scheduled(cron = "0 0 7 * * *")
    public void executeSampleJob() {
        logger.info("Notifying subscribers");

        bot.notifySubscribers();
    }
}