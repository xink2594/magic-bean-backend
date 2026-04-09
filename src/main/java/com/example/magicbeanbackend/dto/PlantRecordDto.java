package com.example.magicbeanbackend.dto;

public record PlantRecordDto(
        Long id,
        Long timestamp,
        Double temperature,
        Double humidity,
        String imageUrl,
        String note) {
}
