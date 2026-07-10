package com.logistics.service;

import com.logistics.dto.DeliveryEventRequest;
import com.logistics.dto.DeliveryEventResponse;

public interface DeliveryEventService {

    DeliveryEventResponse recordEvent(DeliveryEventRequest request);
}
