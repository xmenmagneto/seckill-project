package com.seckill.kafka;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class KafkaSender {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    public void sendOrderMessage(String topic, String message) {
        try {
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, message);

            future.whenComplete((result, ex) -> {
                if (ex != null) {
                    log.error("❌ Kafka 消息发送失败：topic={}, message={}, 错误信息={}", topic, message, ex.getMessage(), ex);
                    // 可选：告警或重试逻辑
                } else {
                    RecordMetadata metadata = result.getRecordMetadata();
                    log.info("✅ Kafka 消息发送成功：topic={}, partition={}, offset={}",
                            metadata.topic(),
                            metadata.partition(),
                            metadata.offset());
                }
            });

        } catch (Exception e) {
            log.error("🚨 Kafka 异常：发送消息时发生异常，topic={}, message={}, 错误信息={}", topic, message, e.getMessage(), e);
        }
    }
}