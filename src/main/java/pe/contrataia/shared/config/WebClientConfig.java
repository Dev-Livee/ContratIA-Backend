package pe.contrataia.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${app.latinfo.base-url}")
    private String latInfoBaseUrl;

    @Value("${app.latinfo.api-key}")
    private String latInfoApiKey;

    @Bean("latInfoWebClient")
    public WebClient latInfoWebClient(WebClient.Builder builder) {
        return builder
                .baseUrl(latInfoBaseUrl)
                .defaultHeader("Authorization", "Bearer " + latInfoApiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }
}
