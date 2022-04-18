package routes

import io.ktor.application.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.ironica.playground.*

@OptIn(ExperimentalSerializationApi::class)
fun Route.getPlaygroundRoute() {
    route("/paidiki-xara") {
        post {
            val data = Json.decodeFromString<Data>(call.receive())
            // see https://github.com/Kotlin/kotlinx.serialization/issues/1419 @StefanoBerlato
            val playgroundInterface = PlaygroundInterface(
                data.code,
                data.grid.map { it.map { it }.toTypedArray() }.toTypedArray(),
                data.players.toTypedArray(),
                data.energy
            )
            try {
                // Need to persist playground in `PlaygroundInterface` so that payloadStorage won't be refreshed
                val resp =
                    withContext(Dispatchers.Default) { playgroundInterface.start() }
                val moves = payloadStorage.get()
//                println("The size of payloads is ${moves.size}")
                when (resp.second) {
                    CodeStatus.OK -> {
                        when (val rsf = resp.first) {
                            is Pair<*, *> -> {
                                call.respond(
                                    NormalMessage(
                                        resp.second,
                                        (rsf as Pair<List<Payload>, GameStatus>).first,
                                        (rsf as Pair<List<Payload>, GameStatus>).second
                                    )
                                )
                            }
                            is String -> {
                                println("Route:: encountered some error \n$rsf\n")
                                call.respond(
                                    ErrorMessage(CodeStatus.ERROR, rsf)
                                )
                            }
                            else -> {
                                throw Exception("PlaygroundRoutes:: this is impossible")
                            }
                        }
                    }
                    CodeStatus.ERROR -> {
                        println("Route:: encountered some error\n${resp.first as String}\n")
                        call.respond(ErrorMessage(
                            resp.second, resp.first as String))
                    }
                    CodeStatus.INCOMPLETE -> {
                        println("Route:: The code is not complete.\n")
                        call.respond(ErrorMessage(
                            resp.second, "incomplete code"))
                    }
                }
            } catch (e: Exception) {
                call.respond(ErrorMessage(CodeStatus.ERROR, e.message ?: ""))
            }
        }
    }
}

fun Application.registerPlaygroundRoutes() {
    routing {
        getPlaygroundRoute()
    }
}