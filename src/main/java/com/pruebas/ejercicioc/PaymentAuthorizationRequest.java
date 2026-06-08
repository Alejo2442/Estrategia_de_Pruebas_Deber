package com.pruebas.ejercicioc;

import java.math.BigDecimal;

public class PaymentAuthorizationRequest {

    private String cardNumber;
    private BigDecimal amount;
    private String currency;

    public PaymentAuthorizationRequest(String cardNumber, BigDecimal amount, String currency) {
        this.cardNumber = cardNumber;
        this.amount = amount;
        this.currency = currency;
    }

    public String getCardNumber() { return cardNumber; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
}
