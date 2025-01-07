package com.spinai.sakgamnono.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {
    
    @Value("${openai.api-key}")
    private String openAIApiKey;
    
    @Value("${openai.base-url}")
    private String openAIBaseUrl;

    @Value("${openai.model}")
    private String defaultModel;

    // Getter 메서드들
    public String getOpenAIApiKey() {
        return openAIApiKey;
    }    
    public String getDefaultModel() {
        return defaultModel;
    }
    public String getOpenAIBaseUrl() {
        return openAIBaseUrl;
    }
}

