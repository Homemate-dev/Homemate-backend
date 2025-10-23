package com.zerobase.homemate.notification.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/webhook/notion")
@RequiredArgsConstructor
public class NotionWebhookController {

    @PostMapping
    public void noticePostHook(
            @RequestHeader(value = "X-Notion-Signature", required = false) String signature,
            @RequestBody byte[] rawBody
    ) {
        log.info("secret key: {}", new String(rawBody));
    }
}
