package com.logistics.integration;

import com.logistics.controller.DriverController;
import com.logistics.dto.UploadResult;
import com.logistics.service.DriverService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DriverController.class)
class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DriverService driverService;

    @Test
    void uploadValidFile_returns201WithResult() throws Exception {
        when(driverService.importDrivers(any()))
                .thenReturn(new UploadResult(3, 0, List.of()));

        var file = new MockMultipartFile("file", "drivers.csv",
                "text/csv", "driverId,name,phone,email,region\nd1,Alice,,, NORTH".getBytes());

        mockMvc.perform(multipart("/api/v1/drivers/upload").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imported").value(3))
                .andExpect(jsonPath("$.skipped").value(0))
                .andExpect(jsonPath("$.errors").isEmpty());
    }

    @Test
    void uploadWithPartialErrors_returns201WithErrors() throws Exception {
        when(driverService.importDrivers(any()))
                .thenReturn(new UploadResult(2, 1, List.of("Row 3: invalid region 'nowhere'")));

        var file = new MockMultipartFile("file", "drivers.csv",
                "text/csv", "some content".getBytes());

        mockMvc.perform(multipart("/api/v1/drivers/upload").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.imported").value(2))
                .andExpect(jsonPath("$.skipped").value(1))
                .andExpect(jsonPath("$.errors[0]").value("Row 3: invalid region 'nowhere'"));
    }

    @Test
    void uploadEmptyFile_returns400() throws Exception {
        var file = new MockMultipartFile("file", "drivers.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/api/v1/drivers/upload").file(file))
                .andExpect(status().isBadRequest());
    }
}
