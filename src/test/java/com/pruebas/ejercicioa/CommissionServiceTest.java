package com.pruebas.ejercicioa;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("CommissionService — Unit Tests + Mockito")
class CommissionServiceTest {

    @Mock
    private CommissionRepository repository;

    private CommissionService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new CommissionService(repository);
    }

    @Nested
    @DisplayName("Tier rate tests")
    class TierTests {

        @Test
        @DisplayName("Zero amount returns 0.00 commission")
        void zeroAmountReturnsZero() {
            assertThat(service.calculateCommission(new BigDecimal("0")))
                    .isEqualByComparingTo("0.00");
        }

        @Test
        @DisplayName("250 is within tier 1 (3%) -> 7.50")
        void midTier1AppliesThreePercent() {
            assertThat(service.calculateCommission(new BigDecimal("250")))
                    .isEqualByComparingTo("7.50");
        }

        @Test
        @DisplayName("500 is the upper boundary of tier 1 -> 15.00")
        void tier1UpperBoundaryAppliesThreePercent() {
            assertThat(service.calculateCommission(new BigDecimal("500")))
                    .isEqualByComparingTo("15.00");
        }

        @Test
        @DisplayName("500.01 falls into tier 2 (2%)")
        void tier2LowerBoundaryAppliesTwoPercent() {
            BigDecimal amount = new BigDecimal("500.01");
            BigDecimal expected = amount.multiply(new BigDecimal("0.020"))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            assertThat(service.calculateCommission(amount)).isEqualByComparingTo(expected);
        }

        @Test
        @DisplayName("2750 is within tier 2 (2%) -> 55.00")
        void midTier2AppliesTwoPercent() {
            assertThat(service.calculateCommission(new BigDecimal("2750")))
                    .isEqualByComparingTo("55.00");
        }

        @Test
        @DisplayName("5000 is the upper boundary of tier 2 -> 100.00")
        void tier2UpperBoundaryAppliesTwoPercent() {
            assertThat(service.calculateCommission(new BigDecimal("5000")))
                    .isEqualByComparingTo("100.00");
        }

        @Test
        @DisplayName("5000.01 falls into tier 3 (1.5%)")
        void tier3LowerBoundaryAppliesOnePointFivePercent() {
            BigDecimal amount = new BigDecimal("5000.01");
            BigDecimal expected = amount.multiply(new BigDecimal("0.015"))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            assertThat(service.calculateCommission(amount)).isEqualByComparingTo(expected);
        }

        @Test
        @DisplayName("12500 is within tier 3 (1.5%) -> 187.50")
        void midTier3AppliesOnePointFivePercent() {
            assertThat(service.calculateCommission(new BigDecimal("12500")))
                    .isEqualByComparingTo("187.50");
        }

        @Test
        @DisplayName("20000 is the upper boundary of tier 3 -> 300.00")
        void tier3UpperBoundaryAppliesOnePointFivePercent() {
            assertThat(service.calculateCommission(new BigDecimal("20000")))
                    .isEqualByComparingTo("300.00");
        }

        @Test
        @DisplayName("20000.01 falls into tier 4 (1%)")
        void tier4LowerBoundaryAppliesOnePercent() {
            BigDecimal amount = new BigDecimal("20000.01");
            BigDecimal expected = amount.multiply(new BigDecimal("0.010"))
                    .setScale(2, java.math.RoundingMode.HALF_UP);
            assertThat(service.calculateCommission(amount)).isEqualByComparingTo(expected);
        }

        @Test
        @DisplayName("100000 is in tier 4 (1%) -> 1000.00")
        void largeAmountAppliesOnePercent() {
            assertThat(service.calculateCommission(new BigDecimal("100000")))
                    .isEqualByComparingTo("1000.00");
        }
    }

    @Nested
    @DisplayName("Rounding tests (HALF_UP, 2 decimal places)")
    class RoundingTests {

        @ParameterizedTest(name = "amount={0} expects commission={1}")
        @CsvSource({
            "333.34, 10.00",
            "166.67, 5.00",
            "16.67,  0.50",
            "1666.67, 33.33",
            "8333.34, 125.00"
        })
        @DisplayName("Commission rounds HALF_UP to exactly 2 decimal places")
        void commissionRoundsHalfUp(String amount, String expectedCommission) {
            assertThat(service.calculateCommission(new BigDecimal(amount.trim())))
                    .isEqualByComparingTo(new BigDecimal(expectedCommission.trim()));
        }
    }

    @Nested
    @DisplayName("Input validation tests")
    class ValidationTests {

        @Test
        @DisplayName("Null amount throws IllegalArgumentException")
        void nullAmountThrows() {
            assertThatThrownBy(() -> service.calculateCommission(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("null");
        }

        @Test
        @DisplayName("Negative amount throws IllegalArgumentException")
        void negativeAmountThrows() {
            assertThatThrownBy(() -> service.calculateCommission(new BigDecimal("-0.01")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("negative");
        }
    }

    @Nested
    @DisplayName("Repository interaction tests (Mockito)")
    class RepositoryTests {

        @Test
        @DisplayName("calculateAndSave calls repository.save exactly once")
        void calculateAndSaveCallsRepositoryOnce() {
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            service.calculateAndSave(new BigDecimal("1000"));

            verify(repository, times(1)).save(any(Commission.class));
        }

        @Test
        @DisplayName("calculateAndSave persists correct base amount and commission")
        void calculateAndSavePersistsCorrectValues() {
            when(repository.save(any())).thenAnswer(inv -> inv.getArgument(0));
            ArgumentCaptor<Commission> captor = ArgumentCaptor.forClass(Commission.class);

            service.calculateAndSave(new BigDecimal("1000"));

            verify(repository).save(captor.capture());
            Commission captured = captor.getValue();
            assertThat(captured.getBaseAmount()).isEqualByComparingTo("1000");
            assertThat(captured.getCommissionAmount()).isEqualByComparingTo("20.00");
            assertThat(captured.getCalculatedAt()).isNotNull();
        }

        @Test
        @DisplayName("calculateCommission does not touch the repository")
        void pureCalculationDoesNotInteractWithRepository() {
            service.calculateCommission(new BigDecimal("500"));
            verifyNoInteractions(repository);
        }

        @Test
        @DisplayName("Repository exception propagates to the caller")
        void repositoryExceptionPropagates() {
            when(repository.save(any())).thenThrow(new RuntimeException("Storage failure"));

            assertThatThrownBy(() -> service.calculateAndSave(new BigDecimal("500")))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Storage failure");
        }
    }
}
