package com.automeds.util;

import com.automeds.exception.BadRequestException;
import com.automeds.exception.FileStorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
/**
 * // EDUCATIONAL CODE EXPLANATION
 * Class: FileStorageUtil
 * Description: Application component class containing configuration, exceptions, or scheduling logic.
 */
public class FileStorageUtil {

    private final Path fileStorageLocation;

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("pdf", "jpg", "jpeg", "png");

    public FileStorageUtil(@Value("${file.upload-dir:uploads/prescriptions}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where uploaded files will be stored.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        String originalFileName = "prescription";
        String name = file.getOriginalFilename();
        if (name != null) {
            originalFileName = StringUtils.cleanPath(name);
        }
        
        String fileExtension = "";
        int i = originalFileName.lastIndexOf('.');
        if (i > 0) {
            fileExtension = originalFileName.substring(i + 1).toLowerCase();
        }

        if (!ALLOWED_EXTENSIONS.contains(fileExtension)) {
            throw new BadRequestException("Invalid file type. Only PDF, JPG, JPEG, and PNG files are allowed.");
        }

        String storedFileName = UUID.randomUUID().toString() + "_" + originalFileName;

        try {
            if (storedFileName.contains("..")) {
                throw new BadRequestException("Filename contains invalid path sequence " + storedFileName);
            }

            Path targetLocation = this.fileStorageLocation.resolve(storedFileName);
            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING);
            }

            return storedFileName;
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file " + storedFileName + ". Please try again!", ex);
        }
    }

    public Path getFilePath(String fileName) {
        return this.fileStorageLocation.resolve(fileName).normalize();
    }
}
