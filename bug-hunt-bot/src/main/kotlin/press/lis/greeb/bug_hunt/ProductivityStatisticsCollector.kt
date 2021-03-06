package press.lis.greeb.bug_hunt

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.github.kittinunf.fuel.httpGet
import java.nio.charset.Charset

/**
 * @author Aleksandr Eliseev
 */
class ProductivityStatisticsCollector(private val cookie: String) {

    fun getWeekProductivityStatistics(): String {
        val (_, _, result) = "https://workflowy.com/get_initialization_data?client_version=21"
                .httpGet()
                .header("cookie", cookie)
                .response()

        val parsedJson = Parser.default().parse(result.get().inputStream(), Charset.forName("UTF8")) as JsonObject

        parsedJson.obj("user")

        val rootProjectArray = parsedJson
                .obj("projectTreeData")
                ?.obj("mainProjectTreeInfo")
                ?.array<JsonObject>("rootProjectChildren")


        val weeklyPlanning = rootProjectArray
                ?.filter { it.string("nm") == "Work" }
                ?.map {
                    it.array<JsonObject>("ch")
                            ?.filter { x -> x.string("nm") == "Недельное планирование" }
                            ?.get(0)
                }
                ?.get(0)

        val currentWeek = weeklyPlanning
                ?.array<JsonObject>("ch")
                ?.get(0)

        val currentWeekDays = currentWeek
                ?.array<JsonObject>("ch")

        val tagsPerDay = currentWeekDays?.map {
            Pair(it.string("nm"),
                    it.array<JsonObject>("ch")?.flatMap { sub ->
                        "#[a-zA-Z0-9]+".toRegex().findAll(sub.string("nm").toString())
                                .toList().flatMap { r -> r.groupValues }
                    }?.groupBy
                    { pair ->
                        pair
                    }?.mapValues { v ->
                        v.value.size
                    })
        }

        tagsPerDay?.forEach { (day, tags) ->
            if (day != null && tags != null) {
                println("$day:")

                tags.forEach { (tag, number) ->
                    println("$tag: $number")
                }

                println("\n")
            }
        }

        val statisticsStringBuilder = StringBuilder(1024)
        statisticsStringBuilder.appendln("Week Productivity Statistics:")

        tagsPerDay?.forEach { (day, tags) ->
            if (day != null && tags != null) {
                statisticsStringBuilder.appendln("$day:")

                tags.forEach { (tag, number) ->
                    statisticsStringBuilder.appendln("$tag: $number")
                }

                statisticsStringBuilder.appendln("\n")
            }
        }

        return statisticsStringBuilder.toString()
    }
}

fun main(args: Array<String>) {
    val cookie = args[0]
    val statisticsString = ProductivityStatisticsCollector(cookie)
            .getWeekProductivityStatistics()

    print(statisticsString)
}