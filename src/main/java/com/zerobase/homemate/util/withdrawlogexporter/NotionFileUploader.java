package com.zerobase.homemate.util.withdrawlogexporter;

import com.fasterxml.jackson.databind.JsonNode;
import com.zerobase.homemate.util.withdrawlogexporter.dto.CreatePageRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.io.File;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotionFileUploader {

    private static final String NOTION_API_FILE_UPLOAD_URL = "https://api.notion.com/v1/file_uploads";
    private static final String NOTION_API_PAGE_URL = "https://api.notion.com/v1/pages";
    private static final String NOTION_API_VERSION_HEADER = "Notion-Version";
    private static final String NOTION_API_VERSION = "2025-09-03";
    private static final int MAX_RETRY_COUNT = 3;

    private final RestClient restClient;

    @Value("${notion.api.secret}")
    private String notionApiToken;
    @Value("${notion.api.database}")
    private String databaseId;

    public void uploadFile(File file) {
        log.info("Uploading file - {}", file.getName());

        int retryCount = 0;

        while (retryCount < MAX_RETRY_COUNT) {
            try {
                String uploadUrl = getUploadUrl();
                String fileId = getFileId(uploadUrl, file);
                uploadFile(fileId);

                log.info("Notion upload succeed");
                return;
            } catch (RestClientResponseException e) {
                log.warn("error occurred while uploading withdraw_log: {}", e.getMessage());
                retryCount++;
                String header = e.getResponseHeaders() != null ?
                        e.getResponseHeaders().getFirst("Retry-After") : null;
                int retryAfter = header != null ? Integer.parseInt(header) : 3;
                sleep(retryAfter);
            }
        }
    }

    private String getUploadUrl() {
        JsonNode response = restClient.post()
                .uri(NOTION_API_FILE_UPLOAD_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + notionApiToken)
                .header(NOTION_API_VERSION_HEADER, NOTION_API_VERSION)
                .contentType(MediaType.APPLICATION_JSON)
                .retrieve()
                .body(JsonNode.class);

        return response.get("upload_url").asText();
    }

    private String getFileId(String uploadUrl, File file) {
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", new FileSystemResource(file));

        JsonNode response = restClient.post()
                .uri(uploadUrl)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + notionApiToken)
                .header(NOTION_API_VERSION_HEADER, NOTION_API_VERSION)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        return response.get("id").asText();
    }

    private void uploadFile(String fileId) {
        CreatePageRequest requestBody = CreatePageRequest.of(databaseId, "탈퇴사유 로그", fileId);

        restClient.post()
                .uri(NOTION_API_PAGE_URL)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + notionApiToken)
                .header(NOTION_API_VERSION_HEADER, NOTION_API_VERSION)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .toBodilessEntity();
    }

    private void sleep(int seconds) {
        try {
            Thread.sleep(seconds * 1000L);
        } catch (InterruptedException ignored) {
        }
    }
}
