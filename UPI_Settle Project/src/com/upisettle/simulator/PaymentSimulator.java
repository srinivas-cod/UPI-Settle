package com.upisettle.simulator;

import com.upisettle.model.PaymentMethod;
import com.upisettle.model.PaymentStatus;
import com.upisettle.model.PaymentTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class PaymentSimulator {

    private final Random random = new Random();

    public List<PaymentTransaction> generateTransactions(
            String merchantId,
            int count) {

        List<PaymentTransaction> transactions =
                new ArrayList<>();

        for (int i = 1; i <= count; i++) {

            String transactionId =
                    String.format("TXN%04d", i);

            BigDecimal amount =
                    BigDecimal.valueOf(
                            50 + random.nextInt(4951)
                    );

            PaymentStatus status =
                    generateStatus();

            PaymentMethod method =
                    generatePaymentMethod();

            PaymentTransaction transaction =
                    new PaymentTransaction(
                            transactionId,
                            merchantId,
                            amount,
                            status,
                            method,
                            LocalDateTime.now()
                    );

            transactions.add(transaction);
        }

        return transactions;
    }

    private PaymentStatus generateStatus() {

        int value = random.nextInt(100);

        if (value < 80) {
            return PaymentStatus.SUCCESS;

        } else if (value < 90) {
            return PaymentStatus.PENDING;

        } else if (value < 97) {
            return PaymentStatus.FAILED;

        } else {
            return PaymentStatus.REFUNDED;
        }
    }

    private PaymentMethod generatePaymentMethod() {

        PaymentMethod[] methods =
                PaymentMethod.values();

        return methods[
                random.nextInt(methods.length)
                ];
    }
}