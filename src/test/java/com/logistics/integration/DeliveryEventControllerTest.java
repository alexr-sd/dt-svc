package com.logistics.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.common.DriverNotFoundException;
import com.logistics.controller.DeliveryEventController;
import com.logistics.dto.DeliveryEventResponse;
import com.logistics.entity.DeliveryStatus;
import com.logistics.service.DeliveryEventService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DeliveryEventController.class)
class DeliveryEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private DeliveryEventService deliveryEventService;

    @Test
    void validRequest_returns201WithResponse() throws Exception {
        var response = new DeliveryEventResponse(1L, "pkg-1", "d-1", DeliveryStatus.DELIVERED,
                LocalDateTime.of(2024, 5, 1, 10, 0));
        when(deliveryEventService.recordEvent(any())).thenReturn(response);

        var body = Map.of(
                "packageId", "pkg-1",
                "driverId", "d-1",
                "status", "DELIVERED",
                "timestamp", "2024-05-01T10:00:00"
        );

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.packageId").value("pkg-1"))
                .andExpect(jsonPath("$.status").value("DELIVERED"));
    }

    @Test
    void missingPackageId_returns400() throws Exception {
        var body = Map.of(
                "driverId", "d-1",
                "status", "DELIVERED",
                "timestamp", "2024-05-01T10:00:00"
        );

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void unknownDriver_returns404() throws Exception {
        when(deliveryEventService.recordEvent(any()))
                .thenThrow(new DriverNotFoundException("d-unknown"));

        var body = Map.of(
                "packageId", "pkg-1",
                "driverId", "d-unknown",
                "status", "DELIVERED",
                "timestamp", "2024-05-01T10:00:00"
        );

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    void missingStatus_returns400() throws Exception {
        var body = Map.of(
                "packageId", "pkg-1",
                "driverId", "d-1",
                "timestamp", "2024-05-01T10:00:00"
        );

        mockMvc.perform(post("/api/v1/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest());
    }
}
