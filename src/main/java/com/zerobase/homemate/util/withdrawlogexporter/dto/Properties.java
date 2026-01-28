package com.zerobase.homemate.util.withdrawlogexporter.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Properties(
        @JsonProperty("이름")
        TitleProperty title,

        @JsonProperty("탈퇴사유 원본데이터")
        FilesProperty files
) {
        public static Properties of(String title, String fileId) {
                return new Properties(
                        TitleProperty.of(title),
                        FilesProperty.of(fileId)
                );
        }
}
