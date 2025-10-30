package com.zerobase.homemate.notification.controller;

import com.zerobase.homemate.notification.dto.NoticeCreateDto;
import com.zerobase.homemate.notification.service.NotificationService;
import com.zerobase.homemate.notification.service.NotionFetchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Slf4j
@RestController
@RequestMapping("/webhook/notion")
@RequiredArgsConstructor
public class NotionWebhookController {

    private final NotionFetchService notionFetchService;
    private final NotificationService notificationService;

    @Value("${notion.api.verification}")
    private String verificationSecret;

    @PostMapping
    public ResponseEntity<String> noticePostHook(
            @RequestHeader(value = "X-Notion-Signature", required = false) String signature,
            @RequestBody byte[] rawBody
    ) {
        if (!verifySignature(verificationSecret, rawBody, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("invalid signature");
        }

        String pageId;
        try {
            pageId = notionFetchService.parsePageId(rawBody);
            if (pageId == null) {
                return ResponseEntity.ok("not page type");
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("invalid request body");
        }

        NoticeCreateDto fetchResult = notionFetchService.fetchPageInfo(pageId);
        if (fetchResult == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Parse Failed");
        }

        notificationService.createNotice(fetchResult);

        return ResponseEntity.ok("ok");
    }

    private boolean verifySignature(String secret, byte[] rawBody, String signatureHeader) {
        if (signatureHeader == null || !signatureHeader.startsWith("sha256=")) return false;
        String signatureHex = signatureHeader.substring("sha256=".length());
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] computed = mac.doFinal(rawBody);
            String computedHex = bytesToHex(computed);

            return MessageDigest.isEqual(computedHex.getBytes(), signatureHex.getBytes());
        } catch (Exception e) {
            return false;
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) sb.append(String.format("%02x", b));
        return sb.toString();
    }
}
