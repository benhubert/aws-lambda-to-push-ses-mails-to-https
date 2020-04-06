package benhub.aws.sestohttp;

import benhub.aws.sestohttp.api.ApiClient;
import com.amazonaws.services.s3.AmazonS3;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

public class SNSEventHandlerTest {

    private ApiClient apiClient;
    private AmazonS3 s3Client;

    private SNSEventHandler eventHandler;

    @BeforeEach
    public void setUp() {
        apiClient = Mockito.mock(ApiClient.class);
        s3Client = Mockito.mock(AmazonS3.class);
        Mockito.when(s3Client.getObjectAsString(Mockito.any(String.class), Mockito.any(String.class))).thenReturn("some text");

        eventHandler = new SNSEventHandler(apiClient, s3Client, null);
    }

    @Test
    public void test_handleEvent_pushesExtractedDataToAPI() {
        eventHandler.handleEvent(Samples.open("event-1"));
        Mockito.verify(apiClient, Mockito.atLeastOnce())
                .pushMailToApi("c292df2c-d2f2-47c2-9ca8-c84b03389282@any.domain.example.com", "some text");
    }

    @Test
    public void test_handleEvent_shouldDeleteMessageOnSuccess() {
        Mockito.when(apiClient.pushMailToApi(Mockito.anyString(), Mockito.anyString())).thenReturn(true);
        eventHandler.handleEvent(Samples.open("event-1"));
        Mockito.verify(s3Client, Mockito.atLeastOnce())
                .deleteObject("test-bucket-name", "inbound/XSVx2kwXNJdYZYdh4xwffR2BLmq52vRY");
    }

    @Test
    public void test_handleEvent_shouldKeepMessageOnFailure() {
        Mockito.when(apiClient.pushMailToApi(Mockito.anyString(), Mockito.anyString())).thenThrow(new IllegalStateException());
        Assertions.assertThrows(IllegalStateException.class, () -> eventHandler.handleEvent(Samples.open("event-1")));
        Mockito.verify(s3Client, Mockito.never())
                .deleteObject("test-bucket-name", "inbound/XSVx2kwXNJdYZYdh4xwffR2BLmq52vRY");
    }

    @Test
    public void test_handleEvent_shouldKeepMessageOnReject() {
        Mockito.when(apiClient.pushMailToApi(Mockito.anyString(), Mockito.anyString())).thenReturn(false);
        eventHandler.handleEvent(Samples.open("event-1"));
        Mockito.verify(s3Client, Mockito.never())
                .deleteObject("test-bucket-name", "inbound/XSVx2kwXNJdYZYdh4xwffR2BLmq52vRY");
    }

}
