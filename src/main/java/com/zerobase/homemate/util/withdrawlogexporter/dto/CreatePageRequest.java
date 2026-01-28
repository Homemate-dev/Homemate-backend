package com.zerobase.homemate.util.withdrawlogexporter.dto;

public record CreatePageRequest(
        DatabaseParent parent,
        Properties properties
) {
    public static CreatePageRequest of(String databaseId, String title, String fileId) {
        return new CreatePageRequest(
                DatabaseParent.of(databaseId),
                Properties.of(title, fileId)
        );
    }
}
