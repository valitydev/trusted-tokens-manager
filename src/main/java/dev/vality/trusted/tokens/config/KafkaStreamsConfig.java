package dev.vality.trusted.tokens.config;

import dev.vality.trusted.tokens.serde.PaymentSerde;
import dev.vality.trusted.tokens.serde.WithdrawalSerde;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndFailExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;
import java.util.Properties;

@Configuration
@RequiredArgsConstructor
public class KafkaStreamsConfig {

    public static final String PAYMENT_SUFFIX = "-payment";
    public static final String WITHDRAWAL_SUFFIX = "-withdrawal";
    private static final String APP_ID = "trusted-tokens";

    @Value("${kafka.client-id}")
    private String clientId;

    @Value("${kafka.num-stream-threads}")
    private int numStreamThreads;

    @Value("${kafka.stream.retries-attempts}")
    private int retriesAttempts;

    @Value("${kafka.stream.retries-backoff-ms}")
    private int retriesBackoffMs;

    @Value("${kafka.stream.default-api-timeout-ms}")
    private int defaultApiTimeoutMs;

    private final KafkaProperties kafkaProperties;

    @Bean
    public Properties paymentEventStreamProperties() {
        final Map<String, Object> props = kafkaProperties.buildStreamsProperties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APP_ID + PAYMENT_SUFFIX);
        props.put(StreamsConfig.CLIENT_ID_CONFIG, clientId + PAYMENT_SUFFIX);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, PaymentSerde.class);
        addDefaultStreamsProperties(props);
        var properties = new Properties();
        properties.putAll(props);
        return properties;
    }

    @Bean
    public Properties withdrawalEventStreamProperties() {
        final Map<String, Object> props = kafkaProperties.buildStreamsProperties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APP_ID + WITHDRAWAL_SUFFIX);
        props.put(StreamsConfig.CLIENT_ID_CONFIG, clientId + WITHDRAWAL_SUFFIX);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, WithdrawalSerde.class);
        addDefaultStreamsProperties(props);
        var properties = new Properties();
        properties.putAll(props);
        return properties;
    }

    private void addDefaultStreamsProperties(Map<String, Object> props) {
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);
        props.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        props.put(StreamsConfig.NUM_STREAM_THREADS_CONFIG, numStreamThreads);
        props.put(StreamsConfig.RETRIES_CONFIG, retriesAttempts);
        props.put(StreamsConfig.RETRY_BACKOFF_MS_CONFIG, retriesBackoffMs);
        props.put(StreamsConfig.DEFAULT_DESERIALIZATION_EXCEPTION_HANDLER_CLASS_CONFIG,
                LogAndFailExceptionHandler.class);
        props.put(ConsumerConfig.DEFAULT_API_TIMEOUT_MS_CONFIG, defaultApiTimeoutMs);
    }
}
