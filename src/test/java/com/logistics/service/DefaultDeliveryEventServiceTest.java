package com.logistics.service;

import com.logistics.common.DriverNotFoundException;
import com.logistics.dto.DeliveryEventRequest;
import com.logistics.dto.DeliveryEventResponse;
import com.logistics.entity.DeliveryEvent;
import com.logistics.entity.DeliveryStatus;
import com.logistics.entity.Driver;
import com.logistics.entity.Region;
import com.logistics.event.delivery.DeliveryEventCreated;
import com.logistics.repository.DeliveryEventRepository;
import com.logistics.repository.DriverRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultDeliveryEventServiceTest {

    @Mock private DeliveryEventRepository eventRepository;
    @Mock private DriverRepository driverRepository;
    @Mock private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private DefaultDeliveryEventService service;

    private final Driver driver = new Driver("D1", "Alice", "555", "alice@mail.com", Region.NORTH);

    @Test
    void throwsWhenDriverNotFound() {
        when(driverRepository.findById("D99")).thenReturn(Optional.empty());

        DeliveryEventRequest request = new DeliveryEventRequest(
                "PKG-1", "D99", DeliveryStatus.PICKED_UP, LocalDateTime.now()
        );

        assertThatThrownBy(() -> service.recordEvent(request))
                .isInstanceOf(DriverNotFoundException.class)
                .hasMessageContaining("D99");

        verify(eventRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void savesEventWithCorrectFields() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 1, 15, 10, 30);
        DeliveryEventRequest request = new DeliveryEventRequest(
                "PKG-1", "D1", DeliveryStatus.DELIVERED, timestamp
        );
        when(driverRepository.findById("D1")).thenReturn(Optional.of(driver));
        when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.recordEvent(request);

        ArgumentCaptor<DeliveryEvent> captor = ArgumentCaptor.forClass(DeliveryEvent.class);
        verify(eventRepository).save(captor.capture());

        DeliveryEvent saved = captor.getValue();
        assertThat(saved.getPackageId()).isEqualTo("PKG-1");
        assertThat(saved.getDriver()).isEqualTo(driver);
        assertThat(saved.getStatus()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(saved.getTimestamp()).isEqualTo(timestamp);
    }

    @Test
    void publishesEventAfterSave() {
        when(driverRepository.findById("D1")).thenReturn(Optional.of(driver));
        when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        service.recordEvent(new DeliveryEventRequest(
                "PKG-1", "D1", DeliveryStatus.IN_TRANSIT, LocalDateTime.now()
        ));

        verify(eventPublisher).publishEvent(any(DeliveryEventCreated.class));
    }

    @Test
    void returnsResponseWithCorrectFields() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 1, 15, 10, 30);
        when(driverRepository.findById("D1")).thenReturn(Optional.of(driver));
        when(eventRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DeliveryEventResponse response = service.recordEvent(new DeliveryEventRequest(
                "PKG-1", "D1", DeliveryStatus.DELIVERED, timestamp
        ));

        assertThat(response.packageId()).isEqualTo("PKG-1");
        assertThat(response.driverId()).isEqualTo("D1");
        assertThat(response.status()).isEqualTo(DeliveryStatus.DELIVERED);
        assertThat(response.timestamp()).isEqualTo(timestamp);
    }
}
