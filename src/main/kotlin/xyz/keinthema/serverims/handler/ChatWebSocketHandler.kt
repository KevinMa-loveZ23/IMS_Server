package xyz.keinthema.serverims.handler

import org.springframework.stereotype.Component
import org.springframework.web.reactive.socket.WebSocketHandler
import org.springframework.web.reactive.socket.WebSocketSession
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class ChatWebSocketHandler: WebSocketHandler {
    override fun handle(session: WebSocketSession): Mono<Void> {
        val receive = session.receive()
            .map { wsMessage -> wsMessage.payloadAsText }
            .doOnNext { message ->
                message.length
            }
        val output = Flux.just("Hello")
            .map(session::textMessage)
        return session.send(output).and(receive)
    }

}