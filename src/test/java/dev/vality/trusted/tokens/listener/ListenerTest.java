package dev.vality.trusted.tokens.listener;

import com.rbkmoney.testcontainers.annotations.KafkaSpringBootTest;
import com.rbkmoney.testcontainers.annotations.kafka.KafkaTestcontainer;
import com.rbkmoney.testcontainers.annotations.kafka.config.KafkaProducer;
import dev.vality.damsel.fraudbusters.Payment;
import dev.vality.damsel.fraudbusters.PaymentStatus;
import dev.vality.damsel.fraudbusters.Withdrawal;
import dev.vality.damsel.fraudbusters.WithdrawalStatus;
import dev.vality.trusted.tokens.config.MockedStartupInitializers;
import dev.vality.trusted.tokens.model.CardTokenData;
import dev.vality.trusted.tokens.repository.CardTokenRepository;
import dev.vality.trusted.tokens.utils.CardTokenDataUtils;
import org.apache.thrift.TBase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;

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
@KafkaSpringBootTest
@Import(MockedStartupInitializers.class)
public class ListenerTest {

    @Value("${kafka.topics.payment.id}")
    private String paymentTopicName;
    @Value("${kafka.topics.withdrawal.id}")
    private String withdrawalTopicName;
    @Autowired
    private KafkaProducer<TBase<?, ?>> testThriftKafkaProducer;
    @MockBean
    private CardTokenRepository cardTokenRepository;

    @BeforeEach
    void setUp() {
        reset(cardTokenRepository);
    }

    @Test
    void listenExistedCapturedPayment() {
        Payment payment = createPayment().setStatus(PaymentStatus.captured);
        String token = payment.getPaymentTool().getBankCard().getToken();
        CardTokenData cardTokenData = CardTokenDataUtils.createCardTokenData();
        cardTokenData.setLastPaymentId(payment.getId());
        when(cardTokenRepository.get(token)).thenReturn(cardTokenData);

        testThriftKafkaProducer.send(paymentTopicName, payment);

        verify(cardTokenRepository, timeout(5000).times(1)).get(token);
        verifyNoMoreInteractions(cardTokenRepository);

    }

    @Test
    void listenOldCapturedPayment() {
        Payment payment = createPayment().setStatus(PaymentStatus.captured);
        String token = payment.getPaymentTool().getBankCard().getToken();
        CardTokenData cardTokenData = CardTokenDataUtils.createCardTokenData();
        when(cardTokenRepository.get(token)).thenReturn(cardTokenData);

        testThriftKafkaProducer.send(paymentTopicName, payment);

        verify(cardTokenRepository, timeout(5000).times(2)).create(any());

    }

    @Test
    void listenNewCapturedPayment() {
        testThriftKafkaProducer.send(paymentTopicName, createPayment().setStatus(PaymentStatus.captured));
        testThriftKafkaProducer.send(paymentTopicName, createPayment().setStatus(PaymentStatus.processed));

        verify(cardTokenRepository, timeout(5000).times(1)).create(any());
    }

    @Test
    void listenExistedCapturedWithdrawal() {
        Withdrawal withdrawal = createWithdrawal().setStatus(WithdrawalStatus.succeeded);
        String token = withdrawal.getDestinationResource().getBankCard().getToken();
        CardTokenData cardTokenData = CardTokenDataUtils.createCardTokenData();
        cardTokenData.setLastWithdrawalId(withdrawal.getId());
        when(cardTokenRepository.get(token)).thenReturn(cardTokenData);

        testThriftKafkaProducer.send(withdrawalTopicName, withdrawal);

        verify(cardTokenRepository, timeout(5000).times(1)).get(token);
        verifyNoMoreInteractions(cardTokenRepository);
    }

    @Test
    void listenOldCapturedWithdrawal() {
        Withdrawal withdrawal = createWithdrawal().setStatus(WithdrawalStatus.succeeded);
        String token = withdrawal.getDestinationResource().getBankCard().getToken();
        CardTokenData cardTokenData = CardTokenDataUtils.createCardTokenData();
        when(cardTokenRepository.get(token)).thenReturn(cardTokenData);

        testThriftKafkaProducer.send(withdrawalTopicName, withdrawal);

        verify(cardTokenRepository, timeout(5000).times(2)).create(any());

    }

    @Test
    void listenSucceededWithdrawal() {
        testThriftKafkaProducer.send(withdrawalTopicName, createWithdrawal().setStatus(WithdrawalStatus.succeeded));
        testThriftKafkaProducer.send(withdrawalTopicName, createWithdrawal().setStatus(WithdrawalStatus.pending));

        verify(cardTokenRepository, timeout(5000).times(1)).create(any());
    }
}
