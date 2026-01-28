package com.zerobase.homemate.util.withdrawlogexporter;

import com.zerobase.homemate.entity.WithdrawLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.List;
import java.util.StringJoiner;

@Component
@Slf4j
public class CsvFileExporter {

    private static final String FILENAME_PREFIX = "withdraw_log_";
    private static final String FILENAME_EXTENSION = ".csv";
    private static final String HEADER = "ID,kakaoID,Reason,Detail,WithdrawDate";

    @Value("${app.csv.export-path:./logs/export}")
    private String exportPath;

    public File exportWithdrawLog(List<WithdrawLog> logs) {
        String filename = FILENAME_PREFIX + LocalDate.now() + FILENAME_EXTENSION;
        Path path = Paths.get(exportPath, filename);

        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }

            try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8)) {
                writer.write('\uFEFF'); // UTF-8 BOM
                writer.write(HEADER);
                writer.newLine();

                for (WithdrawLog l : logs) {
                    StringJoiner sj = new StringJoiner(",");
                    sj.add(String.valueOf(l.getId()));
                    sj.add(l.getProviderUserId());
                    sj.add(escapeSpecialCharacters(l.getReason()));
                    sj.add(escapeSpecialCharacters(l.getDetail()));
                    sj.add(l.getCreatedAt().toString());

                    writer.write(sj.toString());
                    writer.newLine();
                }
            }

            log.info("CSV file created successfully with {} data at {}", logs.size(), path.toAbsolutePath());
            return path.toFile();

        } catch (IOException e) {
            log.error("CSV file creation failed", e);
            throw new RuntimeException("CSV export failed", e);
        }
    }

    private String escapeSpecialCharacters(String text) {
        if (text == null) {
            return "";
        }

        String escapedData = text.replaceAll("\\R", " ");
        if (text.contains(",") || text.contains("\"") || text.contains("'")) {
            text = text.replace("\"", "\"\"");
            escapedData = "\"" + text + "\"";
        }
        return escapedData;
    }
}
