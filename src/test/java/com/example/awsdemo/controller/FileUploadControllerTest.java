package com.example.awsdemo.controller;

import io.awspring.cloud.s3.S3Template;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileUploadControllerTest {

    private static final String BUCKET = "test-bucket";

    @Mock
    private S3Template s3Template;

    private FileUploadController controller;

    @BeforeEach
    void setUp() {
        // @InjectMocks can't supply the String bucket name (it would pass null),
        // so build the controller explicitly.
        this.controller = new FileUploadController(this.s3Template, BUCKET);
    }

    @Test
    void uploadFile_shouldCallS3TemplateAndReturnConfirmation() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "dummy content".getBytes()
        );

        String result = this.controller.uploadFile(file);

        assertThat(result).isEqualTo("Uploaded: test.pdf");
        verify(this.s3Template, times(1)).upload(eq(BUCKET), eq("test.pdf"), any());
    }
}
