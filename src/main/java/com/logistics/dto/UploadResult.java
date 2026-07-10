package com.logistics.dto;

import java.util.List;

public record UploadResult(int imported, int skipped, List<String> errors) {
}
