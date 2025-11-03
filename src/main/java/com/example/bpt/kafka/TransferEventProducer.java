package com.example.bpt.kafka;

import com.example.bpt.event.TransferCompletedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferEventProducer {

    private final KafkaTemplate<String, TransferCompletedEvent> kafkaTemplate;
    private static final String TOPIC = "transfer-events";

    public void sendTransferCompletedEvent(TransferCompletedEvent event) {
        log.info("Sending Kafka event to topic {} -> {}", TOPIC, event);
        kafkaTemplate.send(TOPIC, event);
    }
}
