package org.example.megasegashop.product.service.image;

import org.example.megasegashop.product.model.Image;
import org.example.megasegashop.product.model.Product;
import org.example.megasegashop.product.repository.ImageRepository;
import org.example.megasegashop.product.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.sql.rowset.serial.SerialBlob;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@Slf4j
@Service
public class ImageService {
    private static final long MAX_IMAGE_BYTES = DataSize.ofMegabytes(5).toBytes();

    private final ImageRepository imageRepository;
    private final ProductRepository productRepository;

    public ImageService(ImageRepository imageRepository, ProductRepository productRepository) {
        this.imageRepository = imageRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public Image uploadImage(Long productId, MultipartFile file) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found"));
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Image file is required");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw new ResponseStatusException(HttpStatus.PAYLOAD_TOO_LARGE, "Image exceeds 5 MB limit");
        }

        Image image = new Image();
        image.setFileName(file.getOriginalFilename());
        image.setFileType(file.getContentType());
        image.setProduct(product);

        try {
            image.setImage(new SerialBlob(file.getBytes()));
        } catch (IOException | SQLException ex) {
            log.error("Failed to read image content: {}", ex.getMessage());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid image payload");
        }

        Image saved = imageRepository.save(image);
        saved.setDownloadUrl("/api/images/" + saved.getId());
        return imageRepository.save(saved);
    }

    public Image getImage(Long imageId) {
        return imageRepository.findById(imageId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Image not found"));
    }

    public List<Image> getImagesByProduct(Long productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return imageRepository.findByProductId(productId);
    }

    @Transactional
    public void deleteImage(Long imageId) {
        Image image = getImage(imageId);
        imageRepository.delete(image);
    }
}
