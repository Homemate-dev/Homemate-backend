package com.zerobase.homemate.badge;

import com.zerobase.homemate.entity.enums.BadgeType;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class BadgeInitializer {

    @Value("${auth.dev.enabled:false}")
    private boolean devEnabled;

    @PostConstruct
    public void init() {
        BadgeType.setDevEnabled(devEnabled);
    }
}
