package com.reservationsystem.payment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    void getAllPayments_paymentsExist_returnsAllPayments() {
        PaymentEntity payment = new PaymentEntity(
                UUID.randomUUID(),
                BigDecimal.valueOf(150),
                "RON",
                "idem-key-1"
        );

        when(paymentRepository.findAll()).thenReturn(List.of(payment));

        List<Payment> result = paymentService.getAllPayments();

        assertThat(result)
                .hasSize(1)
                .extracting(Payment::idempotencyKey)
                .containsExactly("idem-key-1");
    }

    @Test
    void getAllPayments_noPayments_returnsEmptyList() {
        when(paymentRepository.findAll()).thenReturn(List.of());

        List<Payment> result = paymentService.getAllPayments();

        assertThat(result).isEmpty();
    }
}
