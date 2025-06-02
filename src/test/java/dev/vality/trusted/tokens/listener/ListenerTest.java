package dev.vality.trusted.tokens.listener;

import dev.vality.damsel.fraudbusters.Payment;
import dev.vality.damsel.fraudbusters.PaymentStatus;
import dev.vality.damsel.fraudbusters.Withdrawal;
import dev.vality.damsel.fraudbusters.WithdrawalStatus;
import dev.vality.testcontainers.annotations.kafka.KafkaTestcontainer;
import dev.vality.testcontainers.annotations.kafka.config.KafkaProducer;
import dev.vality.testcontainers.annotations.kafka.config.KafkaProducerConfig;
import dev.vality.testcontainers.annotations.postgresql.PostgresqlTestcontainer;
import dev.vality.trusted.tokens.dao.TokenDao;
import dev.vality.trusted.tokens.model.CardTokenData;
import dev.vality.trusted.tokens.utils.CardTokenDataUtils;
import org.apache.thrift.TBase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static dev.vality.trusted.tokens.utils.TransactionUtils.createPayment;
import static dev.vality.trusted.tokens.utils.TransactionUtils.createWithdrawal;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@KafkaTestcontainer(
        properties = {
                "kafka.topics.payment.consume.enabled=true",
                "kafka.topics.withdrawal.consume.enabled=true",
                "kafka.state.cache.size=0"},
        topicsKeys = {
                "kafka.topics.payment.id",
                "kafka.topics.withdrawal.id",
                "kafka.topics.payment.dest",
                "kafka.topics.withdrawal.dest"})
@PostgresqlTestcontainer
@SpringBootTest
@ContextConfiguration(classes = {KafkaProducerConfig.class})
public class ListenerTest {

    @Value("${kafka.topics.payment.id}")
    private String paymentTopicName;
    @Value("${kafka.topics.withdrawal.id}")
    private String withdrawalTopicName;
    @Autowired
    private KafkaProducer<TBase<?, ?>> testThriftKafkaProducer;
    @MockitoBean
    private TokenDao tokenDao;

    @Test
    void listenExistedCapturedPayment() {
        Payment payment = createPayment().setStatus(PaymentStatus.captured);
        String token = payment.getPaymentTool().getBankCard().getToken();
        CardTokenData cardTokenData = CardTokenDataUtils.createCardTokenData();
        cardTokenData.setLastPaymentId(payment.getId());
        when(tokenDao.get(token)).thenReturn(cardTokenData);

        testThriftKafkaProducer.send(paymentTopicName, payment);

        verify(tokenDao, timeout(10000).times(1)).get(token);
        verifyNoMoreInteractions(tokenDao);

    }

    @Test
    void listenOldCapturedPayment() {
        Payment payment = createPayment().setStatus(PaymentStatus.captured);
        String token = payment.getPaymentTool().getBankCard().getToken();
        CardTokenData cardTokenData = CardTokenDataUtils.createCardTokenData();
        when(tokenDao.get(token)).thenReturn(cardTokenData);

        testThriftKafkaProducer.send(paymentTopicName, payment);

        verify(tokenDao, timeout(10000).times(2)).create(any());

    }

    @Test
    void listenNewCapturedPayment() {
        testThriftKafkaProducer.send(paymentTopicName, createPayment().setStatus(PaymentStatus.captured));
        testThriftKafkaProducer.send(paymentTopicName, createPayment().setStatus(PaymentStatus.processed));

        verify(tokenDao, timeout(10000).times(1)).create(any());
    }

    @Test
    void listenExistedCapturedWithdrawal() {
        Withdrawal withdrawal = createWithdrawal().setStatus(WithdrawalStatus.succeeded);
        String token = withdrawal.getDestinationResource().getBankCard().getToken();
        CardTokenData cardTokenData = CardTokenDataUtils.createCardTokenData();
        cardTokenData.setLastWithdrawalId(withdrawal.getId());
        when(tokenDao.get(token)).thenReturn(cardTokenData);

        testThriftKafkaProducer.send(withdrawalTopicName, withdrawal);

        verify(tokenDao, timeout(10000).times(1)).get(token);
        verifyNoMoreInteractions(tokenDao);
    }

    @Test
    void listenOldCapturedWithdrawal() {
        Withdrawal withdrawal = createWithdrawal().setStatus(WithdrawalStatus.succeeded);
        String token = withdrawal.getDestinationResource().getBankCard().getToken();
        CardTokenData cardTokenData = CardTokenDataUtils.createCardTokenData();
        when(tokenDao.get(token)).thenReturn(cardTokenData);

        testThriftKafkaProducer.send(withdrawalTopicName, withdrawal);

        verify(tokenDao, timeout(10000).times(2)).create(any());

    }

    @Test
    void listenSucceededWithdrawal() {
        testThriftKafkaProducer.send(withdrawalTopicName, createWithdrawal().setStatus(WithdrawalStatus.succeeded));
        testThriftKafkaProducer.send(withdrawalTopicName, createWithdrawal().setStatus(WithdrawalStatus.pending));

        verify(tokenDao, timeout(10000).times(1)).create(any());
    }
}
