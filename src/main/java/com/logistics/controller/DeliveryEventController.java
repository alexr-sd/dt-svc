package com.logistics.controller;

import com.logistics.dto.DeliveryEventRequest;
import com.logistics.dto.DeliveryEventResponse;
import com.logistics.service.DeliveryEventService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/events")
public class DeliveryEventController {

    private final DeliveryEventService eventService;

    DeliveryEventController(DeliveryEventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<DeliveryEventResponse> recordEvent(@RequestBody @Valid DeliveryEventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.recordEvent(request));
    }
}
