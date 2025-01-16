// 나중에 로컬 서버 실행시키기 전 테스트용 코드임
package com.spinai.sakgamnono.service;

import com.spinai.sakgamnono.config.OpenAIConfig;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Service
public class OpenAIService {
    
    private final OpenAIConfig openAIConfig;
    private final RestTemplate restTemplate;

    public OpenAIService(OpenAIConfig openAIConfig) {
        this.openAIConfig = openAIConfig;
        this.restTemplate = new RestTemplate();
    }

    public String callOpenAI(String userInput) {
        // 1) 필요한 값을 config에서 가져오기
        String apiKey = openAIConfig.getOpenAIApiKey();
        String baseUrl = openAIConfig.getOpenAIBaseUrl();
        String model = openAIConfig.getDefaultModel();

        String url = baseUrl + "/chat/completions";

        // 2) 헤더
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        // 3) 메시지
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);

        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", userInput));
        requestBody.put("messages", messages);

        HttpEntity<Map<String, Object>> request = new HttpEntity<>(requestBody, headers);

        // 4) API 요청
        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                Map body = response.getBody();
                List<Map> choices = (List<Map>) body.get("choices");
                if (choices != null && !choices.isEmpty()) {
                    Map firstChoice = choices.get(0);
                    Map messageObj = (Map) firstChoice.get("message");
                    if (messageObj != null) {
                        return (String) messageObj.get("content");
                    }
                }
                return "No content in OpenAI response.";
            } else {
                return "Non-200 response from OpenAI: " + response.getStatusCode();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling OpenAI: " + e.getMessage();
        }
    }
}