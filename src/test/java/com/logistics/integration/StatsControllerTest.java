package com.logistics.integration;

import com.logistics.common.StringToMetricTypeConverter;
import com.logistics.common.StringToRegionConverter;
import com.logistics.controller.StatsController;
import com.logistics.dto.StatsResponse;
import com.logistics.service.StatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StatsController.class)
@Import({StringToMetricTypeConverter.class, StringToRegionConverter.class})
class StatsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatsService statsService;

    @Test
    void validRequest_returns200WithStats() throws Exception {
        when(statsService.getStats(any(), any(), any(), any(), any()))
                .thenReturn(new StatsResponse("total_packages", 5.0,
                        Map.of("driverIds", List.of(), "regions", List.of()),
                        Map.of("from", "2024-05-01", "to", "2024-05-07")));

        mockMvc.perform(get("/api/v1/stats")
                        .param("metric", "total_packages")
                        .param("from", "2024-05-01")
                        .param("to", "2024-05-07"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.metric").value("total_packages"))
                .andExpect(jsonPath("$.value").value(5.0));
    }

    @Test
    void missingMetric_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/stats")
                        .param("from", "2024-05-01")
                        .param("to", "2024-05-07"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidMetric_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/stats")
                        .param("metric", "unknown_metric")
                        .param("from", "2024-05-01")
                        .param("to", "2024-05-07"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void dateRangeViolation_returns400() throws Exception {
        when(statsService.getStats(any(), any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Date range must not exceed 31 days"));

        mockMvc.perform(get("/api/v1/stats")
                        .param("metric", "total_packages")
                        .param("from", "2024-01-01")
                        .param("to", "2024-02-10"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Date range must not exceed 31 days"));
    }

    @Test
    void regionFilter_passedThrough() throws Exception {
        when(statsService.getStats(any(), any(), any(), any(), any()))
                .thenReturn(new StatsResponse("delivery_rate", 0.8,
                        Map.of("driverIds", List.of(), "regions", List.of("north")),
                        Map.of("from", "2024-05-01", "to", "2024-05-01")));

        mockMvc.perform(get("/api/v1/stats")
                        .param("metric", "delivery_rate")
                        .param("regions", "north"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.value").value(0.8));
    }
}
