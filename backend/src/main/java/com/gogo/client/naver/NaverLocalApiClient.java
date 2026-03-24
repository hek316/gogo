package com.gogo.client.naver;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.Objects;

@Component
public class NaverLocalApiClient {

    private static final Logger log = LoggerFactory.getLogger(NaverLocalApiClient.class);
    private static final String NAVER_LOCAL_API = "https://openapi.naver.com/v1/search/local.json";
    private static final String HTML_TAG_PATTERN = "<[^>]*>";

    private final String clientId;
    private final String clientSecret;
    private final RestClient restClient;

    public NaverLocalApiClient(
            @Value("${naver.client-id}") String clientId,
            @Value("${naver.client-secret}") String clientSecret) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.restClient = RestClient.create();
    }

    /** client 자체 DTO(NaverSearchResult)를 반환. domain DTO 참조 없음. */
    public List<NaverSearchResult> search(String keyword) {
        if (clientId.isBlank() || clientSecret.isBlank()) {
            log.warn("Naver API credentials not configured. Returning empty results.");
            return List.of();
        }

        try {
            NaverSearchResponse response = restClient.get()
                    .uri(NAVER_LOCAL_API + "?query={keyword}&display=5", keyword)
                    .header("X-Naver-Client-Id", clientId)
                    .header("X-Naver-Client-Secret", clientSecret)
                    .retrieve()
                    .body(NaverSearchResponse.class);

            if (response == null || response.items() == null) {
                return List.of();
            }

            return response.items().stream()
                    .filter(Objects::nonNull)
                    .map(this::toSearchResult)
                    .toList();
        } catch (RestClientException e) {
            log.error("Naver API call failed for keyword '{}': {}", keyword, e.getMessage());
            return List.of();
        } catch (RuntimeException e) {
            log.error("Failed to parse Naver API response for keyword '{}': {}", keyword, e.getMessage(), e);
            return List.of();
        }
    }

    private NaverSearchResult toSearchResult(NaverLocalApiItem item) {
        String name = item.title() != null
                ? item.title().replaceAll(HTML_TAG_PATTERN, "")
                : null;

        String address = (item.roadAddress() != null && !item.roadAddress().isBlank())
                ? item.roadAddress()
                : item.address();

        String category = item.category() != null
                ? item.category().split(">")[0].trim()
                : null;

        String mapUrl = (item.link() != null && !item.link().isBlank())
                ? item.link()
                : null;

        return new NaverSearchResult(name, address, mapUrl, category, item.telephone());
    }

    /** client 자체 결과 DTO */
    public record NaverSearchResult(String name, String address, String mapUrl, String category, String telephone) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record NaverSearchResponse(List<NaverLocalApiItem> items) {}
}
