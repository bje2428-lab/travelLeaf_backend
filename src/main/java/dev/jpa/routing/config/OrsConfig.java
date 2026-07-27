package dev.jpa.routing.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class OrsConfig {

    @Bean
    public RestClient orsRestClient(
            @Value("${ors.base-url}") String baseUrl,
            @Value("${ors.api-key:}") String apiKey
    ) {
        RestClient.Builder builder = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Accept", "application/geo+json");

        // ✅ 키가 있을 때만 헤더 추가 (키 없으면 부팅은 되고, 호출 시에만 에러 처리)
        if (apiKey != null && !apiKey.isBlank()) {
            builder.defaultHeader("Authorization", apiKey);
        }

        return builder.build();
    }
}
