package com.example.awsdemo.controller;

import io.awspring.cloud.s3.S3Template;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileUploadControllerTest {

    @Mock
    private S3Template s3Template;

    @InjectMocks
    private FileUploadController controller;

    @Test
    void uploadFile_shouldCallS3TemplateAndReturnConfirmation() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "dummy content".getBytes()
        );

        String result = this.controller.uploadFile(file);

        assertThat(result).isEqualTo("Uploaded: test.pdf");
        verify(this.s3Template, times(1)).upload(anyString(), eq("test.pdf"), any());
    }
}