package press.lis.greeb.bug_hunt

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

/**
 * @author Aleksandr Eliseev
 */
@Component
class BugHuntBotScheduling(
        private val bugHuntBot: BugHuntBot
) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Scheduled(cron = "0 0 7 * * *")
    fun scheduleUpdate() {
        logger.info("Sending scheduled updates")
        bugHuntBot.sendStatistics()
    }
}