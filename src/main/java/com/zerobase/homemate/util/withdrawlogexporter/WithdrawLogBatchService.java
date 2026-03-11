package com.zerobase.homemate.util.withdrawlogexporter;

import com.zerobase.homemate.entity.WithdrawLog;
import com.zerobase.homemate.repository.WithdrawLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class WithdrawLogBatchService {

    private final WithdrawLogRepository withdrawLogRepository;
    private final CsvFileExporter csvFileExporter;
    private final NotionFileUploader notionFileUploader;

    @Transactional
    public void executeBatchProcess(LocalDateTime start, LocalDateTime end) {
        List<WithdrawLog> logs = withdrawLogRepository.findByCreatedAtBetweenOrderByIdAsc(start, end);
        log.info("{} rows fetched", logs.size());

        if (logs.isEmpty()) {
            log.info("No data to export - skip exporting");
            return;
        }

        File csvFile = csvFileExporter.exportWithdrawLog(logs);

        if (csvFile != null && csvFile.exists()) {
            notionFileUploader.uploadFile(csvFile);
        }
    }
}
