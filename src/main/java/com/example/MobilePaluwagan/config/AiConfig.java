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

    // --- Primary ---
    @Value("${spring.ai.openai.api-key}")
    private String primaryApiKey;

    @Value("${spring.ai.openai.base-url}")
    private String baseUrl;

    @Value("${spring.ai.openai.chat.options.model}")
    private String model;
    // Fallback 2
    @Value("${groq.fallback.api-key}")
    private String fallbackApiKey;

    @Value("${groq.fallback.base-url}")
    private String fallbackBaseUrl;

    @Value("${groq.fallback.model}")
    private String fallbackModel;

    // Fallback 3
    @Value("${groq.fallback2.api-key}")
    private String fallback3ApiKey;

    @Value("${groq.fallback2.base-url}")
    private String fallback3BaseUrl;

    @Value("${groq.fallback2.model}")
    private String fallback3Model;

    // Fallback 4
    @Value("${groq.fallback3.api-key}")
    private String fallback4ApiKey;

    @Value("${groq.fallback3.base-url}")
    private String fallback4BaseUrl;

    @Value("${groq.fallback3.model}")
    private String fallback4Model;

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
    // FALLBACK 2 BEANS
    // =====================
    @Bean("fallback2OpenAiApi")
    public OpenAiApi fallback2OpenAiApi() {
        return OpenAiApi.builder()
                .apiKey(fallbackApiKey)
                .baseUrl(fallbackBaseUrl)
                .build();
    }

    @Bean("fallback2OpenAiChatModel")
    public OpenAiChatModel fallback2OpenAiChatModel(
            @Qualifier("fallback2OpenAiApi") OpenAiApi openAiApi) {
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
    // FALLBACK 3 BEANS
    // =====================
    @Bean("fallback3OpenAiApi")
    public OpenAiApi fallback3OpenAiApi() {
        return OpenAiApi.builder()
                .apiKey(fallback3ApiKey)
                .baseUrl(fallback3BaseUrl)
                .build();
    }

    @Bean("fallback3OpenAiChatModel")
    public OpenAiChatModel fallback3OpenAiChatModel(
            @Qualifier("fallback3OpenAiApi") OpenAiApi openAiApi) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(fallback3Model)
                .store(false)
                .streamUsage(false)
                .build();
        return OpenAiChatModel.builder()
                .openAiApi(openAiApi)
                .defaultOptions(options)
                .build();
    }

    // =====================
    // FALLBACK 4 BEANS
    // =====================
    @Bean("fallback4OpenAiApi")
    public OpenAiApi fallback4OpenAiApi() {
        return OpenAiApi.builder()
                .apiKey(fallback4ApiKey)
                .baseUrl(fallback4BaseUrl)
                .build();
    }

    @Bean("fallback4OpenAiChatModel")
    public OpenAiChatModel fallback4OpenAiChatModel(
            @Qualifier("fallback4OpenAiApi") OpenAiApi openAiApi) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .model(fallback4Model)
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
            @Qualifier("primaryOpenAiChatModel") OpenAiChatModel primaryModel) {
        return ChatClient.builder(primaryModel).build();
    }

    @Bean("groqFallback2ChatClient")
    public ChatClient groqFallback2ChatClient(
            @Qualifier("fallback2OpenAiChatModel") OpenAiChatModel fallbackModel) {
        return ChatClient.builder(fallbackModel).build();
    }

    @Bean("groqFallback3ChatClient")
    public ChatClient groqFallback3ChatClient(
            @Qualifier("fallback3OpenAiChatModel") OpenAiChatModel fallbackModel) {
        return ChatClient.builder(fallbackModel).build();
    }

    @Bean("groqFallback4ChatClient")
    public ChatClient groqFallback4ChatClient(
            @Qualifier("fallback4OpenAiChatModel") OpenAiChatModel fallbackModel) {
        return ChatClient.builder(fallbackModel).build();
    }

    @Bean("geminiChatClient")
    public ChatClient geminiChatClient(GoogleGenAiChatModel geminiModel) {
        return ChatClient.builder(geminiModel).build();
    }
}
