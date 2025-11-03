package com.example.bpt.kafka;

import com.example.bpt.event.TransferCompletedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class TransferEventConsumer {

    @KafkaListener(topics = "transfer-events", groupId = "bpt-consumer-group")
    public void consumeTransferCompletedEvent(TransferCompletedEvent event) {
        log.info("✅ Kafka message received → Source: {}, Destination: {}, Amount: {}, Timestamp: {}",
                event.sourceAccount(), event.destinationAccount(), event.amount(), event.timestamp());
    }
}
