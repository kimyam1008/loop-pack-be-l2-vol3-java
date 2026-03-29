package com.loopers.application.queue;

public class QueueDto {

    public record QueueEntryResult(
            Long position,
            Long totalWaiting,
            Long estimatedWaitSeconds
    ) {
        public static QueueEntryResult of(Long position, Long totalWaiting, Long estimatedWaitSeconds) {
            return new QueueEntryResult(position, totalWaiting, estimatedWaitSeconds);
        }
    }

    public record QueuePositionResult(
            Long position,
            Long totalWaiting,
            Long estimatedWaitSeconds,
            String token
    ) {
        public static QueuePositionResult of(Long position, Long totalWaiting, Long estimatedWaitSeconds, String token) {
            return new QueuePositionResult(position, totalWaiting, estimatedWaitSeconds, token);
        }
    }
}
