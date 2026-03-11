package com.zerobase.homemate.util.withdrawlogexporter.dto;

import java.util.List;

public record TitleProperty(
        List<TitleItem> title
) {
    public static TitleProperty of(String title) {
        return new TitleProperty(
                List.of(new TitleItem(new TitleItem.TitleText(title)))
        );
    }

    record TitleItem(
            TitleText text
    ) {
        record TitleText(
                String content
        ) {
        }
    }
}
