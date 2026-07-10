package com.logistics.repository;

import com.logistics.domain.DeliveryEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface DeliveryEventRepository extends JpaRepository<DeliveryEvent, Long> {

    /**
     * For each package, returns the event with the latest timestamp within [from, to].
     * This is the "terminal state" of a package for the given date range.
     */
    @Query("""
            SELECT e FROM DeliveryEvent e
            JOIN FETCH e.driver
            WHERE e.timestamp = (
                SELECT MAX(e2.timestamp) FROM DeliveryEvent e2
                WHERE e2.packageId = e.packageId
                  AND e2.timestamp >= :from
                  AND e2.timestamp <= :to
            )
            AND e.timestamp >= :from
            AND e.timestamp <= :to
            """)
    List<DeliveryEvent> findTerminalEventsInRange(
            @Param("from") LocalDateTime from,
            @Param("to") LocalDateTime to
    );
}
