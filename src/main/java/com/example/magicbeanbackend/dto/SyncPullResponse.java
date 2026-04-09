package com.example.magicbeanbackend.dto;

import java.util.List;

public record SyncPullResponse(List<PlantRecordDto> records) {
}
