package com.zerobase.homemate.util.withdrawlogexporter.dto;

public record DatabaseParent(
        String type,
        String database_id
) {
    public static DatabaseParent of(String databaseId) {
        return new DatabaseParent("database_id", databaseId);
    }
}
