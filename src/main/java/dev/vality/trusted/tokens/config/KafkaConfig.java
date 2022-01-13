package dev.vality.trusted.tokens.config;

import dev.vality.damsel.fraudbusters.Payment;
import dev.vality.damsel.fraudbusters.Withdrawal;
import dev.vality.trusted.tokens.config.properties.KafkaSslProperties;
import dev.vality.trusted.tokens.serde.deserializer.PaymentDeserializer;
import dev.vality.trusted.tokens.serde.deserializer.WithdrawalDeserializer;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
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
@EnableConfigurationProperties(KafkaSslProperties.class)
public class KafkaConfig {

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.client-id}")
    private String clientId;

    @Value("${kafka.consumer.group-id}")
    private String groupId;

    @Value("${kafka.consumer.max-poll-interval-ms}")
    private int maxPollInterval;

    @Value("${kafka.consumer.max-session-timeout-ms}")
    private int maxSessionTimeout;

    @Value("${kafka.topics.payment.consume.max-poll-records}")
    private String paymentMaxPollRecords;

    @Value("${kafka.topics.payment.consume.concurrency}")
    private int paymentConcurrency;

    @Value("${kafka.topics.withdrawal.consume.max-poll-records}")
    private String withdrawalMaxPollRecords;

    @Value("${kafka.topics.withdrawal.consume.concurrency}")
    private int withdrawalConcurrency;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Payment> paymentListenerContainerFactory(
            KafkaSslProperties kafkaSslProperties) {
        var containerFactory = new ConcurrentKafkaListenerContainerFactory<String, Payment>();
        configureContainerFactory(
                containerFactory,
                new PaymentDeserializer(),
                clientId + "-payment",
                paymentMaxPollRecords,
                kafkaSslProperties);
        containerFactory.setConcurrency(paymentConcurrency);
        return containerFactory;
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Withdrawal> withdrawalListenerContainerFactory(
            KafkaSslProperties kafkaSslProperties) {
        var containerFactory = new ConcurrentKafkaListenerContainerFactory<String, Withdrawal>();
        configureContainerFactory(
                containerFactory,
                new WithdrawalDeserializer(),
                clientId + "-withdrawal",
                withdrawalMaxPollRecords,
                kafkaSslProperties);
        containerFactory.setConcurrency(withdrawalConcurrency);
        return containerFactory;
    }

    private <T> void configureContainerFactory(
            ConcurrentKafkaListenerContainerFactory<String, T> containerFactory,
            Deserializer<T> deserializer,
            String clientId,
            String maxPollRecords,
            KafkaSslProperties kafkaSslProperties) {
        var consumerFactory = createKafkaConsumerFactory(
                deserializer,
                clientId,
                maxPollRecords,
                kafkaSslProperties);
        containerFactory.setConsumerFactory(consumerFactory);
        containerFactory.setBatchErrorHandler(new SeekToCurrentBatchErrorHandler());
        containerFactory.setBatchListener(true);
        containerFactory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL);
    }

    private <T> DefaultKafkaConsumerFactory<String, T> createKafkaConsumerFactory(
            Deserializer<T> deserializer,
            String clientId,
            String maxPollRecords,
            KafkaSslProperties kafkaSslProperties) {
        Map<String, Object> properties = defaultProperties(kafkaSslProperties);
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.put(ConsumerConfig.CLIENT_ID_CONFIG, clientId);
        properties.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, maxPollRecords);
        return new DefaultKafkaConsumerFactory<>(properties, new StringDeserializer(), deserializer);
    }

    private Map<String, Object> defaultProperties(KafkaSslProperties kafkaSslProperties) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, EARLIEST.name().toLowerCase());
        properties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
        properties.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, maxPollInterval);
        properties.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, maxSessionTimeout);
        configureSsl(properties, kafkaSslProperties);
        return properties;
    }

    private void configureSsl(Map<String, Object> properties, KafkaSslProperties kafkaSslProperties) {
        if (kafkaSslProperties.isEnabled()) {
            properties.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, SecurityProtocol.SSL.name());
            properties.put(SslConfigs.SSL_TRUSTSTORE_LOCATION_CONFIG,
                    new File(kafkaSslProperties.getTrustStoreLocation()).getAbsolutePath());
            properties.put(SslConfigs.SSL_TRUSTSTORE_PASSWORD_CONFIG, kafkaSslProperties.getTrustStorePassword());
            properties.put(SslConfigs.SSL_KEYSTORE_TYPE_CONFIG, kafkaSslProperties.getKeyStoreType());
            properties.put(SslConfigs.SSL_TRUSTSTORE_TYPE_CONFIG, kafkaSslProperties.getTrustStoreType());
            properties.put(SslConfigs.SSL_KEYSTORE_LOCATION_CONFIG,
                    new File(kafkaSslProperties.getKeyStoreLocation()).getAbsolutePath());
            properties.put(SslConfigs.SSL_KEYSTORE_PASSWORD_CONFIG, kafkaSslProperties.getKeyStorePassword());
            properties.put(SslConfigs.SSL_KEY_PASSWORD_CONFIG, kafkaSslProperties.getKeyPassword());
        }
    }
}
