package com.example.MobilePaluwagan.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Value("${app.cors.allowed-cors}")
    private String allowedOrigins;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config){
        // dto naka sub yung admin and member 1 to 1 by ticket:
        // /topic/chat/ticketId
        config.enableSimpleBroker("/topic");

        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry){
        // websocket connection para sa frontend ws://54.251.224.183:8080/ws
        registry.addEndpoint("/ws")
                .setAllowedOrigins(allowedOrigins.split(","))
                .withSockJS();
    }
}
