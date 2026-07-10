package com.logistics.controller;

import com.logistics.dto.UploadResult;
import com.logistics.service.DriverService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/drivers")
class DriverController {

    private final DriverService driverService;

    DriverController(DriverService driverService) {
        this.driverService = driverService;
    }

    @PostMapping("/upload")
    public ResponseEntity<UploadResult> upload(@RequestParam("file") MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(driverService.importDrivers(file));
    }
}
