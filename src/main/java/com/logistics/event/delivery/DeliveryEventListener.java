package com.logistics.event.delivery;

import com.logistics.entity.DeliveryStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class DeliveryEventListener {

    private static final Logger log = LoggerFactory.getLogger(DeliveryEventListener.class);

    @EventListener
    public void onDeliveryEvent(DeliveryEventCreated event) {
        var delivery = event.event();
        if (delivery.getStatus() == DeliveryStatus.FAILED) {
            log.warn("FAILED delivery — package={} driver={} at={}",
                    delivery.getPackageId(),
                    delivery.getDriver().getDriverId(),
                    delivery.getTimestamp());
        }
    }
}
