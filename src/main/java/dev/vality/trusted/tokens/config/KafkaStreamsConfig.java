package dev.vality.trusted.tokens.config;

import dev.vality.trusted.tokens.config.properties.KafkaSslProperties;
import dev.vality.trusted.tokens.serde.PaymentSerde;
import dev.vality.trusted.tokens.serde.WithdrawalSerde;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.config.SslConfigs;
import org.apache.kafka.common.security.auth.SecurityProtocol;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.errors.LogAndFailExceptionHandler;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Configuration
@EnableConfigurationProperties(KafkaSslProperties.class)
public class KafkaStreamsConfig {

    public static final String PAYMENT_SUFFIX = "-payment";
    public static final String WITHDRAWAL_SUFFIX = "-withdrawal";
    private static final String APP_ID = "trusted-tokens";

    @Value("${kafka.bootstrap-servers}")
    private String bootstrapServers;

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

    @Bean
    public Properties paymentEventStreamProperties(KafkaSslProperties kafkaSslProperties) {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APP_ID + PAYMENT_SUFFIX);
        props.put(StreamsConfig.CLIENT_ID_CONFIG, clientId + PAYMENT_SUFFIX);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, PaymentSerde.class);
        addDefaultStreamsProperties(props);
        props.putAll(configureSsl(kafkaSslProperties));
        return props;
    }

    private Map<String, Object> configureSsl(KafkaSslProperties kafkaSslProperties) {
        Map<String, Object> properties = new HashMap<>();
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
        return properties;
    }

    @Bean
    public Properties withdrawalEventStreamProperties(KafkaSslProperties kafkaSslProperties) {
        final Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, APP_ID + WITHDRAWAL_SUFFIX);
        props.put(StreamsConfig.CLIENT_ID_CONFIG, clientId + WITHDRAWAL_SUFFIX);
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass().getName());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, WithdrawalSerde.class);
        addDefaultStreamsProperties(props);
        props.putAll(configureSsl(kafkaSslProperties));
        return props;
    }

    private void addDefaultStreamsProperties(Properties props) {
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
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
