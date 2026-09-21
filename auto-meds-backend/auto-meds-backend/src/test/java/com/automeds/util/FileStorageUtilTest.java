package com.automeds.util;

import com.automeds.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import org.mockito.Mockito;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class FileStorageUtilTest {

    @TempDir
    Path tempDir;

    private FileStorageUtil fileStorageUtil;

    @BeforeEach
    void setUp() {
        fileStorageUtil = new FileStorageUtil(tempDir.toString());
    }

    @Test
    void testStoreFileSuccess() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "prescription.pdf", "application/pdf", "Dummy content".getBytes()
        );

        String storedName = fileStorageUtil.storeFile(file);
        assertNotNull(storedName);
        assertTrue(storedName.endsWith("_prescription.pdf"));

        Path resolved = fileStorageUtil.getFilePath(storedName);
        assertTrue(resolved.toFile().exists());
    }

    @Test
    void testStoreFileInvalidExtension() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "virus.exe", "application/octet-stream", "bad".getBytes()
        );

        assertThrows(BadRequestException.class, () -> fileStorageUtil.storeFile(file));
    }

    @Test
    void testStoreFileNullFilename() {
        MockMultipartFile file = new MockMultipartFile(
                "file", null, "image/png", "png data".getBytes()
        );

        assertThrows(BadRequestException.class, () -> fileStorageUtil.storeFile(file));
    }

    @Test
    void testStoreFileInvalidPathSequence() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "../prescription.pdf", "application/pdf", "data".getBytes()
        );
        assertThrows(BadRequestException.class, () -> fileStorageUtil.storeFile(file));
    }

    @Test
    void testStoreFileIOException() throws Exception {
        org.springframework.web.multipart.MultipartFile file = Mockito.mock(org.springframework.web.multipart.MultipartFile.class);
        Mockito.when(file.getOriginalFilename()).thenReturn("test.pdf");
        Mockito.when(file.getInputStream()).thenThrow(new java.io.IOException("Disk full"));

        assertThrows(RuntimeException.class, () -> fileStorageUtil.storeFile(file));
    }

    @Test
    void testInvalidDirConstructor() {
        assertThrows(RuntimeException.class, () -> new FileStorageUtil("\0invalid_path"));
    }
}


