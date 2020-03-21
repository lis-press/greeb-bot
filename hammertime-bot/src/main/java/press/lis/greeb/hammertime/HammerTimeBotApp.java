package press.lis.greeb.hammertime;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Aleksandr Eliseev
 */
@ComponentScan
@EnableScheduling
public class HammerTimeBotApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder(HammerTimeBotApp.class)
                .run(args);
    }
}
