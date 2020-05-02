package press.lis.greeb.experiments

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.github.kittinunf.fuel.httpGet
import java.nio.charset.Charset

/**
 * @author Aleksandr Eliseev
 */
fun main(args: Array<String>) {
    val cookie = args[0]
    val (request, response, result) = "https://workflowy.com/get_initialization_data?client_version=21"
            .httpGet()
            .header("cookie", cookie)
            .response()

    val parsedJson = Parser.default().parse(result.get().inputStream(), Charset.forName("UTF8")) as JsonObject

    parsedJson.obj("user")

    val rootProjectArray = parsedJson
            .obj("projectTreeData")
            ?.obj("mainProjectTreeInfo")
            ?.array<JsonObject>("rootProjectChildren")

    print(1)

    print(1.1)

    val weeklyPlanning = rootProjectArray
            ?.filter { it.string("nm") == "Work" }
            ?.map {
                it.array<JsonObject>("ch")
                        ?.filter { x -> x.string("nm") == "Недельное планирование" }
            }

    print(2)

    val currentWeek = weeklyPlanning
            ?.get(0)
            ?.get(0)
            ?.array<JsonObject>("ch")
            ?.get(0)

    val currentWeekDays = currentWeek
            ?.array<JsonObject>("ch")

    print(3)

}