package press.lis.greeb.experiments

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.github.kittinunf.fuel.httpGet
import java.nio.charset.Charset

/**
 * @author Aleksandr Eliseev
 */
fun main(args: Array<String>) {
    val authCode = args[0]
    val (_, _, result) =
            "https://www.wrike.com/api/v4/stream"
                    .httpGet()
                    .header("Authorization", "bearer $authCode")
                    .response()

    val parsedJson = Parser.default().parse(result.get().inputStream(), Charset.forName("UTF8")) as JsonObject

    print(parsedJson)

    // Pagination needed after 1000 entries
    // https://help.wrike.com/hc/en-us/community/posts/211881385-API-Pagination-and-the-nextpage-Token

    print("Nothing here yet :)")
}