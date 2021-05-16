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
            val playgroundInterface = PlaygroundInterface(data.code, convertJsonToGrid(data.grid))
            try {
                playgroundInterface.start()
                val moves = payloadStorage
//                println("The size of payloads is ${moves.size}")
                if (gameStatus == Status.OK) call.respond(NormalMessage(Status.OK, moves))
                else call.respond(ErrorMessage(Status.ERROR, "Something went wrong while processing your request."))
                payloadStorage.clear()
            } catch (e: Exception) {
                call.respond(ErrorMessage(Status.ERROR, e.message ?: ""))
                payloadStorage.clear()
            }
        }
    }
}

fun Application.registerPlaygroundRoutes() {
    routing {
        getPlaygroundRoute()
    }
}