package dev.vality.trusted.tokens.utils;

import dev.vality.testcontainers.annotations.util.ValuesGenerator;
import dev.vality.damsel.domain.BankCard;
import dev.vality.damsel.domain.Cash;
import dev.vality.damsel.domain.CurrencyRef;
import dev.vality.damsel.domain.PaymentTool;
import dev.vality.damsel.fraudbusters.*;

import java.time.LocalDateTime;

public class TransactionUtils {

    public static Payment createPayment() {
        return new Payment()
                .setId(ValuesGenerator.generateId())
                .setEventTime(String.valueOf(LocalDateTime.now()))
                .setReferenceInfo(ReferenceInfo.merchant_info(new MerchantInfo()))
                .setPaymentTool(PaymentTool.bank_card(new BankCard()))
                .setCost(new Cash()
                        .setCurrency(new CurrencyRef().setSymbolicCode("RUB"))
                        .setAmount(1000))
                .setProviderInfo(new ProviderInfo())
                .setStatus(PaymentStatus.processed)
                .setClientInfo(new ClientInfo());
    }

    public static Withdrawal createWithdrawal() {
        return new Withdrawal()
                .setId(ValuesGenerator.generateId())
                .setEventTime(String.valueOf(LocalDateTime.now()))
                .setDestinationResource(Resource.bank_card(new BankCard()))
                .setCost(new Cash()
                        .setCurrency(new CurrencyRef().setSymbolicCode("RUB"))
                        .setAmount(1000))
                .setStatus(WithdrawalStatus.pending)
                .setAccount(new Account());
    }

}
