package dev.vality.trusted.tokens.config;

import dev.vality.damsel.fraudbusters.Payment;
import dev.vality.damsel.fraudbusters.Withdrawal;
import dev.vality.trusted.tokens.serde.deserializer.PaymentDeserializer;
import dev.vality.trusted.tokens.serde.deserializer.WithdrawalDeserializer;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.SeekToCurrentBatchErrorHandler;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.clients.consumer.OffsetResetStrategy.EARLIEST;

@Configuration
@RequiredArgsConstructor
public class KafkaConfig {

    @Value("${spring.kafka.client-id}")
    private String clientId;

    @Value("${kafka.topics.payment.consume.max-poll-records}")
    private String paymentMaxPollRecords;

    @Value("${kafka.topics.payment.consume.concurrency}")
    private int paymentConcurrency;

    @Value("${kafka.topics.withdrawal.consume.max-poll-records}")
    private String withdrawalMaxPollRecords;

    @Value("${kafka.topics.withdrawal.consume.concurrency}")
    private int withdrawalConcurrency;

    private final KafkaProperties kafkaProperties;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Payment> paymentListenerContainerFactory() {
        var containerFactory = new ConcurrentKafkaListenerContainerFactory<String, Payment>();
        configureContainerFactory(
                containerFactory,
                new PaymentDeserializer(),
                clientId + "-payment",
                paymentMaxPollRecords);
        containerFactory.setConcurrency(paymentConcurrency);
        return containerFactory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Withdrawal> withdrawalListenerContainerFactory() {
        var containerFactory = new ConcurrentKafkaListenerContainerFactory<String, Withdrawal>();
        configureContainerFactory(
                containerFactory,
                new WithdrawalDeserializer(),
                clientId + "-withdrawal",
                withdrawalMaxPollRecords);
        containerFactory.setConcurrency(withdrawalConcurrency);
        return containerFactory;
    }

    private <T> void configureContainerFactory(
            ConcurrentKafkaListenerContainerFactory<String, T> containerFactory,
            Deserializer<T> deserializer,
            String clientId,
            String maxPollRecords) {
        var consumerFactory = createKafkaConsumerFactory(
                deserializer,
                clientId,
                maxPollRecords);
        containerFactory.setConsumerFactory(consumerFactory);
        containerFactory.setBatchErrorHandler(new SeekToCurrentBatchErrorHandler());
        containerFactory.setBatchListener(true);
        containerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
    }

    private <T> DefaultKafkaConsumerFactory<String, T> createKafkaConsumerFactory(
            Deserializer<T> deserializer,
            String clientId,
            String maxPollRecords) {
        Map<String, Object> properties = kafkaProperties.buildConsumerProperties();
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), deserializer);
    }

}
