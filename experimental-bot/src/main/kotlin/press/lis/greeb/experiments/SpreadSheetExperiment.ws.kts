package press.lis.greeb.experiments

import com.beust.klaxon.JsonObject
import com.beust.klaxon.Parser
import com.github.kittinunf.fuel.httpGet
import java.nio.charset.Charset

val cookie = "sessionid=p9fpe222ib1ot2wf38xen3ohzfv1dcuf"
val r = "https://workflowy.com/get_initialization_data?client_version=21"
        .httpGet()
        .header("cookie", cookie)
        .response()

val (request, response, result) = r

print(r.first)
print(r.second)
print(r.third)

val parsedJson = Parser.default().parse(r.third.component1()!!.inputStream(), Charset.forName("UTF8")) as JsonObject

parsedJson.obj("user")