package org.example.megasegashop.product.dto;

public record ImageResponse(
        Long id,
        String fileName,
        String fileType,
        String downloadUrl
) {
}
