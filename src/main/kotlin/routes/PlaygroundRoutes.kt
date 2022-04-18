package routes

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receive
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Route
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.routing.routing
import org.ironica.playground.*
import java.lang.Exception

fun Route.getPlaygroundRoute() {
    route("/paidiki-xara") {
        post {
            val data = call.receive<Data>()
            val playgroundInterface = PlaygroundInterface(
                data.code,
                data.grid.map { it.map { it }.toTypedArray() }.toTypedArray(),
                data.players.toTypedArray()
            )
            try {
                val status = playgroundInterface.start()
                val moves = payloadStorage.get()
//                println("The size of payloads is ${moves.size}")
                when (status) {
                    Status.OK -> call.respond(NormalMessage(Status.OK, moves))
                    Status.ERROR -> call.respond(ErrorMessage(Status.ERROR, "Something went wrong while processing your request."))
                    Status.INCOMPLETE -> call.respond(ErrorMessage(Status.INCOMPLETE, "It seems like you have not entered the full program."))
                }
            } catch (e: Exception) {
                call.respond(ErrorMessage(Status.ERROR, e.message ?: ""))
            }
        }
    }
}

fun Application.registerPlaygroundRoutes() {
    routing {
        getPlaygroundRoute()
    }
}