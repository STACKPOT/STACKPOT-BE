package stackpot.stackpot.pot.service.pot;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import stackpot.stackpot.config.OpenAIConfig;

import java.util.Collections;
import java.util.Map;

@Service
public class PotSummarizationService {
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/chat/completions";
    private final RestTemplate restTemplate;
    private final OpenAIConfig openAIConfig;

    public PotSummarizationService(RestTemplate restTemplate, OpenAIConfig openAIConfig) {
        this.restTemplate = restTemplate;
        this.openAIConfig = openAIConfig;
    }

    public String summarizeText(String text, int maxTokens) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(openAIConfig.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> requestBody = Map.of(
                "model", "gpt-4-turbo",
                "messages", Collections.singletonList(
                        Map.of("role", "user", "content", text)
                ),
                "max_tokens", maxTokens
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<Map> response = restTemplate.exchange(OPENAI_API_URL, HttpMethod.POST, entity, Map.class);

        Map<String, Object> responseBody = response.getBody();
        if (responseBody != null && responseBody.containsKey("choices")) {
            Map<String, Object> choice = (Map<String, Object>) ((java.util.List<?>) responseBody.get("choices")).get(0);
            Map<String, String> message = (Map<String, String>) choice.get("message");
            return message.get("content");
        }
        throw new RuntimeException("Failed to summarize text");
    }
}