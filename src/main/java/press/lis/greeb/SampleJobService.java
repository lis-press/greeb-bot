package press.lis.greeb;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class SampleJobService {

    private static final Logger logger = LoggerFactory.getLogger(SampleJobService.class);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");


    @Scheduled(fixedRate = 5000)
    public void executeSampleJob() {
        logger.info("The time is now {}", dateFormat.format(new Date()));

    }
}