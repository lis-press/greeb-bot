package press.lis.greeb.bug_hunt

import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.ComponentScan
import org.springframework.scheduling.annotation.EnableScheduling

/**
 * @author Aleksandr Eliseev
 */
@ComponentScan
@EnableScheduling
class SpringApp

fun main(args: Array<String>) {
    SpringApplicationBuilder(SpringApp::class.java)
            .run(*args)
}