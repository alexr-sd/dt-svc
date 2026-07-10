package com.logistics.service;

import com.logistics.dto.UploadResult;
import org.springframework.web.multipart.MultipartFile;

public interface DriverService {

    UploadResult importDrivers(MultipartFile file);
}
