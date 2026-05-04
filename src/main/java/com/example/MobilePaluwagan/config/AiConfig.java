package com.example.MobilePaluwagan.config;

import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AiConfig {

    // --- Primary Groq ---
    @Value("${spring.ai.openai.api-key}")
    private String primaryApiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;

    // --- Fallback Groq ---
    @Value("${groq.fallback.api-key}")
    private String fallbackApiKey;

    @Value("${groq.fallback.base-url}")
    private String fallbackBaseUrl;

    @Value("${groq.fallback.model}")
    private String fallbackModel;

    // =====================
    // PRIMARY GROQ BEANS
    // =====================
    @Bean("primaryOpenAiApi")
    public OpenAiApi primaryOpenAiApi() {
        return OpenAiApi.builder()
                .apiKey(primaryApiKey)
                .baseUrl(baseUrl)
                .build();
    }

    @Bean("primaryOpenAiChatModel")
    public OpenAiChatModel primaryOpenAiChatModel(
            @Qualifier("primaryOpenAiApi") OpenAiApi openAiApi) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(model)
                .store(false)
                .streamUsage(false)
                .build();
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }

    // =====================
    // FALLBACK GROQ BEANS
    // =====================
    @Bean("fallbackOpenAiApi")
    public OpenAiApi fallbackOpenAiApi() {
        return OpenAiApi.builder()
                .apiKey(fallbackApiKey)
                .baseUrl(fallbackBaseUrl)
                .build();
    }

    @Bean("fallbackOpenAiChatModel")
    public OpenAiChatModel fallbackOpenAiChatModel(
            @Qualifier("fallbackOpenAiApi") OpenAiApi openAiApi) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(fallbackModel)
                .store(false)
                .streamUsage(false)
                .build();
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }

    // =====================
    // CHAT CLIENTS
    // =====================
    @Bean("groqChatClient")
    public ChatClient groqChatClient(
            @Qualifier("primaryOpenAiChatModel") OpenAiChatModel primaryModel,
            @Qualifier("fallbackOpenAiChatModel") OpenAiChatModel fallbackModel) {
        return ChatClient.builder(primaryModel).build();
    }

    @Bean("groqFallbackChatClient")
    public ChatClient groqFallbackChatClient(
            @Qualifier("fallbackOpenAiChatModel") OpenAiChatModel fallbackModel) {
        return ChatClient.builder(fallbackModel).build();
    }

    @Bean("geminiChatClient")
    public ChatClient geminiChatClient(GoogleGenAiChatModel geminiModel) {
        return ChatClient.builder(geminiModel).build();
    }

}
