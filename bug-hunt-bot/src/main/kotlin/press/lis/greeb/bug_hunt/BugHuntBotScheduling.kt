package press.lis.greeb.bug_hunt

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct

/**
 * @author Aleksandr Eliseev
 */
@Component
class BugHuntBotScheduling(
        private val bugHuntBot: BugHuntBot,
        private val productivityStatisticsCollector: ProductivityStatisticsCollector
) {
    private val logger: Logger = LoggerFactory.getLogger(this.javaClass)

    @Scheduled(cron = "0 0 7 * * *")
    fun scheduleUpdate() {
        logger.info("Sending scheduled updates")

        try {
            val weekStatistics = productivityStatisticsCollector.getWeekProductivityStatistics()
            bugHuntBot.sendCustomMessage(weekStatistics)
        } catch (e: Exception) {
            logger.warn("Failed to send weekly statistics", e)
        }

        bugHuntBot.sendStatistics()
    }

    @PostConstruct
    fun scheduleOnInit() {
        // TODO made to initialize currentRowIndexed correctly and make deployment procedure easier (BugHuntBot)
        // TODO not sure should be included in final interface
        scheduleUpdate()
    }
}