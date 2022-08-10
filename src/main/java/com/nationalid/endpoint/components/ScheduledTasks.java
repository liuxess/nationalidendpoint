package com.nationalid.endpoint.components;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.nationalid.endpoint.service.NationalIDService;

import lombok.RequiredArgsConstructor;
import nationalid.loggers.LogManager;

/**
 * Hosts various tasks that have to be ran periodically
 */
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final NationalIDService nationalIDService;

    /**
     * Every 2 minutes deletes from the DB IDs that are incorrect
     */
    @Scheduled(fixedRate = 2 * 60 * 1000) // minutes * seconds_in_minute * milliseconds_in_second
    public void deleteIDsWithErrors() {
        LogManager.getGlobalInstance().LogMessage("Scheduled Task: deleting incorrectIDs");
        int deleted = nationalIDService.deleteAllIncorrect();
        LogManager.getGlobalInstance().LogMessage(String.format("Deleted %d incorrect IDs", deleted));
    }
}
