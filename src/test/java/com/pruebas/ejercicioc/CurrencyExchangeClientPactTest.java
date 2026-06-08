package com.pruebas.ejercicioc;

import au.com.dius.pact.consumer.MockServer;
import au.com.dius.pact.consumer.dsl.PactDslJsonBody;
import au.com.dius.pact.consumer.dsl.PactDslWithProvider;
import au.com.dius.pact.consumer.junit5.PactConsumerTestExt;
import au.com.dius.pact.consumer.junit5.PactTestFor;
import au.com.dius.pact.core.model.PactSpecVersion;
import au.com.dius.pact.core.model.RequestResponsePact;
import au.com.dius.pact.core.model.annotations.Pact;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(PactConsumerTestExt.class)
@PactTestFor(providerName = "PaymentProvider", pactVersion = PactSpecVersion.V3)
@DisplayName("PaymentClient — Pact Consumer Contract Tests")
class CurrencyExchangeClientPactTest {

    @Pact(consumer = "PaymentConsumer", provider = "PaymentProvider")
    RequestResponsePact authorizeApproved(PactDslWithProvider builder) {
        return builder
                .given("existe una tarjeta válida con fondos suficientes")
                .uponReceiving("POST /payments/authorize con monto válido")
                    .path("/payments/authorize")
                    .method("POST")
                    .headers("Content-Type", "application/json")
                    .body(new PactDslJsonBody()
                            .stringType("cardNumber", "4111111111111111")
                            .decimalType("amount", 100.00)
                            .stringType("currency", "USD"))
                .willRespondWith()
                    .status(200)
                    .headers(Map.of("Content-Type", "application/json"))
                    .body(new PactDslJsonBody()
                            .stringType("authorizationCode", "AUTH-EXAMPLE")
                            .stringMatcher("status", "APPROVED|PENDING", "APPROVED")
                            .decimalType("approvedAmount", 100.00))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "authorizeApproved")
    @DisplayName("contrato: autorización aprobada devuelve APPROVED")
    void testAuthorizeApproved(MockServer mockServer) {
        PaymentClient client = new PaymentClient(mockServer.getUrl());

        PaymentAuthorizationRequest request =
                new PaymentAuthorizationRequest("4111111111111111", new BigDecimal("100.00"), "USD");

        PaymentAuthorizationResponse response = client.authorize(request);

        assertThat(response.getStatus()).matches("APPROVED|PENDING");
        assertThat(response.getAuthorizationCode()).isNotBlank();
        assertThat(response.getApprovedAmount()).isNotNull().isPositive();
    }

    @Pact(consumer = "PaymentConsumer", provider = "PaymentProvider")
    RequestResponsePact authorizeDeclined(PactDslWithProvider builder) {
        return builder
                .given("la tarjeta no tiene fondos suficientes")
                .uponReceiving("POST /payments/authorize con monto excesivo")
                    .path("/payments/authorize")
                    .method("POST")
                    .headers("Content-Type", "application/json")
                    .body(new PactDslJsonBody()
                            .stringType("cardNumber", "4000000000000002")
                            .decimalType("amount", 99999.00)
                            .stringType("currency", "USD"))
                .willRespondWith()
                    .status(422)
                    .headers(Map.of("Content-Type", "application/json"))
                    .body(new PactDslJsonBody()
                            .stringMatcher("status", "DECLINED", "DECLINED")
                            .stringType("reason", "INSUFFICIENT_FUNDS"))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "authorizeDeclined")
    @DisplayName("contrato: fondos insuficientes lanza PaymentDeclinedException")
    void testAuthorizeDeclined(MockServer mockServer) {
        PaymentClient client = new PaymentClient(mockServer.getUrl());

        PaymentAuthorizationRequest request =
                new PaymentAuthorizationRequest("4000000000000002", new BigDecimal("99999.00"), "USD");

        assertThatThrownBy(() -> client.authorize(request))
                .isInstanceOf(PaymentDeclinedException.class);
    }

    @Pact(consumer = "PaymentConsumer", provider = "PaymentProvider")
    RequestResponsePact authorizeWithExtraFields(PactDslWithProvider builder) {
        return builder
                .given("el proveedor devuelve campos adicionales")
                .uponReceiving("POST /payments/authorize — respuesta con campos extra")
                    .path("/payments/authorize")
                    .method("POST")
                    .headers("Content-Type", "application/json")
                    .body(new PactDslJsonBody()
                            .stringType("cardNumber", "4111111111111111")
                            .decimalType("amount", 50.00)
                            .stringType("currency", "EUR"))
                .willRespondWith()
                    .status(200)
                    .headers(Map.of("Content-Type", "application/json"))
                    .body(new PactDslJsonBody()
                            .stringType("authorizationCode", "AUTH-XYZ")
                            .stringType("status", "APPROVED")
                            .decimalType("approvedAmount", 50.00)
                            .stringType("processorId", "VISA-EU")
                            .integerType("processingTimeMs", 42))
                .toPact();
    }

    @Test
    @PactTestFor(pactMethod = "authorizeWithExtraFields")
    @DisplayName("contrato: campos adicionales del proveedor son ignorados (tolerant reader)")
    void testTolerantReader(MockServer mockServer) {
        PaymentClient client = new PaymentClient(mockServer.getUrl());

        PaymentAuthorizationRequest request =
                new PaymentAuthorizationRequest("4111111111111111", new BigDecimal("50.00"), "EUR");

        PaymentAuthorizationResponse response = client.authorize(request);

        assertThat(response.getStatus()).isEqualTo("APPROVED");
        assertThat(response.getAuthorizationCode()).isEqualTo("AUTH-XYZ");
    }
}
