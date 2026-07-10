package com.logistics.service;

import com.logistics.common.DriverNotFoundException;
import com.logistics.dto.DeliveryEventRequest;
import com.logistics.dto.DeliveryEventResponse;
import com.logistics.entity.DeliveryEvent;
import com.logistics.entity.Driver;
import com.logistics.event.delivery.DeliveryEventCreated;
import com.logistics.repository.DeliveryEventRepository;
import com.logistics.repository.DriverRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
class DefaultDeliveryEventService implements DeliveryEventService {

    private final DeliveryEventRepository eventRepository;
    private final DriverRepository driverRepository;
    private final ApplicationEventPublisher eventPublisher;

    DefaultDeliveryEventService(
            DeliveryEventRepository eventRepository,
            DriverRepository driverRepository,
            ApplicationEventPublisher eventPublisher
    ) {
        this.eventRepository = eventRepository;
        this.driverRepository = driverRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public DeliveryEventResponse recordEvent(DeliveryEventRequest request) {
        Driver driver = driverRepository.findById(request.driverId())
                .orElseThrow(() -> new DriverNotFoundException(request.driverId()));

        DeliveryEvent event = new DeliveryEvent(
                request.packageId(),
                driver,
                request.status(),
                request.timestamp()
        );

        DeliveryEvent saved = eventRepository.save(event);
        eventPublisher.publishEvent(new DeliveryEventCreated(saved));

        return DeliveryEventResponse.from(saved);
    }
}
