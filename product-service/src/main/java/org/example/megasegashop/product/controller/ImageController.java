package org.example.megasegashop.product.controller;

import org.example.megasegashop.product.dto.ImageResponse;
import org.example.megasegashop.product.model.Image;
import org.example.megasegashop.product.service.image.ImageService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;

@RestController
@RequestMapping
public class ImageController {
    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @PostMapping("/products/{productId}/images")
    public ImageResponse upload(
            @PathVariable Long productId,
            @RequestParam("file") MultipartFile file
    ) {
        Image image = imageService.uploadImage(productId, file);
        return toResponse(image);
    }

    @GetMapping("/products/{productId}/images")
    public List<ImageResponse> list(@PathVariable Long productId) {
        return imageService.getImagesByProduct(productId).stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/images/{imageId}")
    public ResponseEntity<byte[]> download(@PathVariable Long imageId) {
        Image image = imageService.getImage(imageId);
        byte[] content = readBlob(image.getImage());

        MediaType mediaType = MediaType.APPLICATION_OCTET_STREAM;
        if (image.getFileType() != null && !image.getFileType().isBlank()) {
            mediaType = MediaType.parseMediaType(image.getFileType());
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + image.getFileName() + "\"")
                .body(content);
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Void> delete(@PathVariable Long imageId) {
        imageService.deleteImage(imageId);
        return ResponseEntity.noContent().build();
    }

    private ImageResponse toResponse(Image image) {
        String downloadUrl = image.getDownloadUrl();
        if (downloadUrl == null || downloadUrl.isBlank()) {
            downloadUrl = "/api/images/" + image.getId();
        }
        return new ImageResponse(
                image.getId(),
                image.getFileName(),
                image.getFileType(),
                downloadUrl
        );
    }

    private byte[] readBlob(Blob blob) {
        if (blob == null) {
            return new byte[0];
        }
        try {
            return blob.getBytes(1, (int) blob.length());
        } catch (SQLException ex) {
            throw new ResponseStatusException(
                    org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to read image content"
            );
        }
    }
}
