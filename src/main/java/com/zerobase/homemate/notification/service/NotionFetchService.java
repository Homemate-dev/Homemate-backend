package com.zerobase.homemate.notification.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zerobase.homemate.notification.dto.NoticeCreateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotionFetchService {

    private static final String NOTION_API_BLOCK_URL = "https://api.notion.com/v1/blocks/";
    private static final String NOTION_API_PAGE_URL = "https://api.notion.com/v1/pages/";
    private static final String NOTION_API_VERSION_HEADER = "Notion-Version";
    private static final String NOTION_API_VERSION = "2025-09-03";
    private static final int MAX_RETRY_COUNT = 3;

    @Value("${notion.api.secret}")
    private String notionApiToken;

    private final ObjectMapper objectMapper;
    private final RestClient restClient;

    public String parsePageId(byte[] rawBody) throws IOException {
        JsonNode jsonNode = objectMapper.readTree(rawBody);
        JsonNode entity = jsonNode.get("entity");

        String id = entity.get("id").asText();
        String type = entity.get("type").asText();
        if (!"page".equals(type)) {
            log.warn("entity is not page type - id: {}, type: {}", id, type);
            return null;
        }

        return id;
    }

    public NoticeCreateDto fetchPageInfo(String pageId) {
        int retryCount = 0;

        while (retryCount < MAX_RETRY_COUNT) {
            try {
                String title = extractTitle(pageId);
                String message = extractPagePreview(pageId);
                String url = extractUrl(pageId);

                return NoticeCreateDto.builder()
                        .title(title)
                        .message(message)
                        .url(url)
                        .build();
            } catch (RestClientResponseException e) {
                log.warn("error occurred while fetching page info: {}", e.getMessage());
                retryCount++;
                String header = e.getResponseHeaders() != null ?
                        e.getResponseHeaders().getFirst("Retry-After") : null;
                int retryAfter = header != null ? Integer.parseInt(header) : 3;
                sleep(retryAfter);
            }
        }

        log.error("Failed to Fetch Notion page: {}", pageId);
        return null;
    }

    private String extractTitle(String pageId) {
        String pageUrl = NOTION_API_PAGE_URL + pageId + "/properties/title";

        JsonNode titleProperty = restClient.get()
                .uri(pageUrl)
                .header(HttpHeaders.AUTHORIZATION, notionApiToken)
                .header(NOTION_API_VERSION_HEADER, NOTION_API_VERSION)
                .retrieve()
                .body(JsonNode.class);

        String title = titleProperty.get("results").get(0).get("title").get("plain_text").asText();

        return title.isEmpty() ? "(제목 없음)" : title;
    }

    private String extractPagePreview(String pageId) {
        String pageUrl = NOTION_API_BLOCK_URL + pageId + "/children?page_size=10";

        JsonNode body = restClient.get()
                .uri(pageUrl)
                .header(HttpHeaders.AUTHORIZATION, notionApiToken)
                .header(NOTION_API_VERSION_HEADER, NOTION_API_VERSION)
                .retrieve()
                .body(JsonNode.class);

        JsonNode blocks = body.get("results");
        for (JsonNode block : blocks) {
            String type = block.get("type").asText();
            if ("paragraph".equals(type)) {
                JsonNode text = block.get("paragraph").get("rich_text");

                if (text.isArray() && !text.isEmpty()) {
                    String message = text.get(0).get("plain_text").textValue();
                    if (message == null) {
                        continue;
                    }

                    return message.length() > 20 ? message.substring(0, 20) + "..." : message;
                }
            }
        }

        return "(내용 없음)";
    }

    private String extractUrl(String pageId) {
        String pageUrl = NOTION_API_PAGE_URL + pageId;

        JsonNode body = restClient.get()
                .uri(pageUrl)
                .header(HttpHeaders.AUTHORIZATION, notionApiToken)
                .header(NOTION_API_VERSION_HEADER, NOTION_API_VERSION)
                .retrieve()
                .body(JsonNode.class);

        String publicUrl = body.get("public_url").textValue();
        if (publicUrl == null || publicUrl.isEmpty()) {
            return body.get("url").asText();
        }

        return publicUrl;
    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException ignored) {
        }
    }
}
