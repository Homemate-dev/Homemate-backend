package com.zerobase.homemate.util.withdrawlogexporter.dto;

import java.util.List;

public record FilesProperty(
       List<FileItem> files
) {
    record FileItem(
            String type,
            FileUpload file_upload
    ) {
        record FileUpload(
                String id
        ) {}

        public static FileItem of(String fileId) {
            return new FileItem("file_upload", new FileUpload(fileId));
        }
    }

    public static FilesProperty of(String fileId) {
        return new FilesProperty(
                List.of(FileItem.of(fileId))
        );
    }
}
