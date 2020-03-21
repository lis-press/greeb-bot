package press.lis.greeb;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Aleksandr Eliseev
 */
@ComponentScan
@EnableScheduling
public class SpringSchedulingApp {

    public static void main(String[] args) {
        new SpringApplicationBuilder(SpringSchedulingApp.class)
                .run(args);
    }
}
