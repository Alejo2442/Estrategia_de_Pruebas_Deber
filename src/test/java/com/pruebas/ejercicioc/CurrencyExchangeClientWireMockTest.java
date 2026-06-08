package com.pruebas.ejercicioc;

import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockExtension;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.*;

@DisplayName("PaymentClient — WireMock Tests")
class CurrencyExchangeClientWireMockTest {

    @RegisterExtension
    static WireMockExtension wireMock = WireMockExtension.newInstance()
            .options(wireMockConfig().dynamicPort())
            .build();

    private PaymentClient client;

    @BeforeEach
    void setUp() {
        client = new PaymentClient(wireMock.baseUrl());
    }

    @Test
    @DisplayName("autorización exitosa devuelve APPROVED con código")
    void authorize_approved() {
        wireMock.stubFor(post(urlEqualTo("/payments/authorize"))
                .withHeader("Content-Type", equalTo("application/json"))
                .withRequestBody(matchingJsonPath("$.cardNumber"))
                .withRequestBody(matchingJsonPath("$.amount"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "authorizationCode": "AUTH-9X7K2",
                                  "status": "APPROVED",
                                  "approvedAmount": 150.00
                                }
                                """)));

        PaymentAuthorizationRequest request =
                new PaymentAuthorizationRequest("4111111111111111", new BigDecimal("150.00"), "USD");

        PaymentAuthorizationResponse response = client.authorize(request);

        assertThat(response.getStatus()).isEqualTo("APPROVED");
        assertThat(response.getAuthorizationCode()).isEqualTo("AUTH-9X7K2");
        assertThat(response.getApprovedAmount()).isEqualByComparingTo("150.00");

        wireMock.verify(1, postRequestedFor(urlEqualTo("/payments/authorize")));
    }

    @Test
    @DisplayName("respuesta 422 lanza PaymentDeclinedException")
    void authorize_declined_throws() {
        wireMock.stubFor(post(urlEqualTo("/payments/authorize"))
                .willReturn(aResponse()
                        .withStatus(422)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "status": "DECLINED",
                                  "reason": "INSUFFICIENT_FUNDS"
                                }
                                """)));

        PaymentAuthorizationRequest request =
                new PaymentAuthorizationRequest("4000000000000002", new BigDecimal("9999.00"), "USD");

        assertThatThrownBy(() -> client.authorize(request))
                .isInstanceOf(PaymentDeclinedException.class)
                .hasMessageContaining("INSUFFICIENT_FUNDS");
    }

    @Test
    @DisplayName("respuesta 503 lanza excepcion HTTP")
    void authorize_serviceUnavailable() {
        wireMock.stubFor(post(urlEqualTo("/payments/authorize"))
                .willReturn(aResponse()
                        .withStatus(503)
                        .withBody("Service Unavailable")));

        PaymentAuthorizationRequest request =
                new PaymentAuthorizationRequest("4111111111111111", new BigDecimal("100.00"), "USD");

        assertThatThrownBy(() -> client.authorize(request))
                .isNotInstanceOf(PaymentDeclinedException.class);
    }

    @Test
    @DisplayName("respuesta con delay — el cliente espera y recibe la respuesta")
    void authorize_withDelay() {
        wireMock.stubFor(post(urlEqualTo("/payments/authorize"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withFixedDelay(200)
                        .withBody("""
                                {
                                  "authorizationCode": "AUTH-SLOW",
                                  "status": "APPROVED",
                                  "approvedAmount": 50.00
                                }
                                """)));

        PaymentAuthorizationRequest request =
                new PaymentAuthorizationRequest("4111111111111111", new BigDecimal("50.00"), "USD");

        long start = System.currentTimeMillis();
        PaymentAuthorizationResponse response = client.authorize(request);
        long elapsed = System.currentTimeMillis() - start;

        assertThat(response.getStatus()).isEqualTo("APPROVED");
        assertThat(elapsed).isGreaterThanOrEqualTo(200);
    }

    @Test
    @DisplayName("el cliente envia Content-Type application/json")
    void clientSendsJsonContentType() {
        wireMock.stubFor(post(urlEqualTo("/payments/authorize"))
                .willReturn(aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {"authorizationCode":"X","status":"APPROVED","approvedAmount":10}
                                """)));

        client.authorize(new PaymentAuthorizationRequest("4111111111111111",
                new BigDecimal("10"), "EUR"));

        wireMock.verify(postRequestedFor(urlEqualTo("/payments/authorize"))
                .withHeader("Content-Type", WireMock.containing("application/json")));
    }
}
