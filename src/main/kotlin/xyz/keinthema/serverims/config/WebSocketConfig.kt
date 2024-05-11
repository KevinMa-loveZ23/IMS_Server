package xyz.keinthema.serverims.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.reactive.HandlerMapping
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter
import xyz.keinthema.serverims.constant.WebSocketConst
import xyz.keinthema.serverims.handler.ChatWebSocketHandler

@Configuration
class WebSocketConfig(
    private val webSocketConst: WebSocketConst
) {
    @Bean
    fun handlerMapping(handler: ChatWebSocketHandler): HandlerMapping {
//        val map = mapOf(webSocketConst.websocketPathWithUserId to handler)
        val map = mapOf(webSocketConst.websocketPath to handler)
        return SimpleUrlHandlerMapping(map, -1)
    }

    @Bean
    fun handlerAdapter() =  WebSocketHandlerAdapter()
}